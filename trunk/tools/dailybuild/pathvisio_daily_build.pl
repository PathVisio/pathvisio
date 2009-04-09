#!/usr/bin/perl
#
# Simple Daily build script
#
# - make a fresh checkout of pathvisio, bridgedb, pvplugins, wikipathways and cytoscape
# - compile
# - run unit tests
# - if a step goes wrong, send email
#
# Note: this script needs to be run by the user that has
# passwordless ssh access to the target site
#
# If you want to run this from anacron, you'll have to
# make sure root has a key that is on .authorized_hosts on the target
# Pay attention to this, scp fails if you need to enter a password!
#

use warnings;
use strict;
use Data::Dumper;

###############
#   globals
###############

use File::Temp qw / tempfile tempdir /;
use File::Find;
use File::Path;

# If $fCleanCheckout == 1, svn checkout will be done in a fresh temp directory.
# If $fCleanCheckout == 0, svn checkout will be done in $defaultDir.
my $fCleanCheckout = 0;

# these people will be emailed when a problem occurs
my @emails = (
    'martijn.vaniersel@bigcat.unimaas.nl', 
#    'wikipathways-commit@googlegroups.com',    
#    'thomas.kelder@bigcat.unimaas.nl',
    );

my @steps;

# if fSendEmail is false, the email will not be sent but
# displayed on the screen

my $fSendEmail = 1;

my $basedir = "/home/martijn";
my $fnCheckstyle = "$basedir/cs_pathvisio.txt";
my $fnLock = "$basedir/pv_dailybuild_lock";
my $defaultDir = "$basedir/temp2";

# checkout dir
my $dir;

#################
# initialization
#################

if ($fCleanCheckout)
{
	$dir = tempdir ( CLEANUP => 1 );
}
else
{
	$dir = $defaultDir;
	
	mkpath ($dir); # make sure dir exists	
}

#################
#  subroutines
#################

# do step is used to break the daily build in logical steps
# each step has a name, an action and possibly a log file.
# do_step dies if the action fails, but not before adding the result to the 
# @steps array
sub do_step
{
	my %args = @_;
	die "Illegal argument" if !defined $args{action};
	
	eval {
		&{$args{action}}();
	};

	my $error = $@;
	my $result =
		{
			name => $args{name},
			log => $args{log},
			status => ($error ? "FAILURE" : "SUCCESS"),
			error => $error,
		};
	
	push @steps, $result;
		
	#die on error
	if ($error)
	{
		die $error;
	}	
}

# returns true if revision is newer than last time
# takes one hashref like so:
# { repo => "url to repo",
#   wc => "path to working copy",
#   stamp => "stamp file containing last checked revision"
# }
#
# The hashref will be updated with these new fields:
#    new_revision
#    old_revision
#    newer => true if new_revision > old_revision
sub check_revision
{
	my $repo = shift;
	die "Illegal argument" if !defined $repo->{wc};
	die "Illegal argument" if !defined $repo->{stamp};
	
	my $old = 0;
	if (-e $repo->{stamp})
	{
		open IN, $repo->{stamp} or die $!;
		$old = <IN>;
		close IN;
	}
	
	my $new = 0;
	open IN2, "svn info ". $repo->{repo} . "|" or die $!;
	while (my $line = <IN2>)
	{
		if ($line =~ /Revision: (\d+)/) { $new = $1; }
	}
	close IN2;

	$repo->{new_revision} = $new;
	$repo->{old_revision} = $old;
	$repo->{newer} = ($new > $old);
		
	return ($new > $old);
}

# write stamp file containging last checked revision
# takes one hashref like so:
# { 
#   stamp => "stamp file containing last checked revision"
#   new_revision => "latest revision"
# }
sub write_stamp
{
	my $repo = shift;
	die "Illegal argument" if !defined $repo->{new_revision};
	die "Illegal argument" if !defined $repo->{stamp};

	print "Writing stamp to " . $repo->{stamp} . "\n";
	open OUT, "> " . $repo->{stamp} or die $!;
	print OUT $repo->{new_revision};
	close OUT;
}


# takes one hashref like so:
# { repo => "url to repo",
#   wc => "path to working copy",
#   stamp => "stamp file containing last checked revision"
#	log => "log progress of operation"
# }
# makes sure a clean, up-to-date working copy of repo is in wc
# if wc doesn't exist it is created
# if wc is empty a fresh checkout is done
# if wc already contains a working copy it's cleaned and updated
# tries 5 times before giving up
sub checkout_or_update
{
	my $repo = shift;
	die "Illegal argument" if !defined $repo->{wc};
	
	mkpath ($repo->{wc});
	chdir ($repo->{wc});
	
	# clean the working copy if we can. Ignore failure, we'll do a checkout anyway.
	system ("svn-clean .");

	# For some reason, our svn server sometimes fails on the first attempt but then works fine afterwards.
	# So if it fails, retry a few times
	my $retries = 5;
	my $fail_delay = 5;
	my $i = 0;
	unlink $repo->{log};
	while (1)
	{
		$i++;

		eval {
			system ("svn checkout " . $repo->{repo}. " . 2>>" . $repo->{log}) == 0
				or die "svn checkout failed: $?";
		};
		unless ($@) { last; } # break if success
		if ($i == $retries)
		{
			die ("svn checkout failed after $i attempts\n");
		}
		print "SVN co failed, trying again...\n";
		sleep ($fail_delay);
	}
}

# Simplified version of do_step for running shell commands
# instead of name, log and action, takes name, log and cmd where cmd is
# a simple shell command.
# redirection to log will be appended automatically.
sub do_command_step
{
	my %args = @_;
	die "Illegal argument" if !defined $args{cmd};
	
	do_step (
		name => $args{name},
		log => $args{log},
		action => sub
		{
			my $cmd = $args{cmd};
			if (defined $args{log})
			{
				$cmd .= " > " . $args{log}
			}
			# run command
			system ($cmd) == 0 or 
				die ($args{name} . "failed with error code ", $? >> 8, "\n");
		}
	);

}

# scp a directory recursively
# takes three arguments
#
# src
# dest
# log
#
# that will be substituted in a command like this
# scp -r [src] [dest] 2>> [log]
#
# tries 5 times before giving up
sub scp_dir
{
	my %args = @_;
	die "Illegal argument" if !defined $args{src};

	my $src = $args{src};
	my $dest = $args{dest};
	my $log = $args{log};
	
	#securely copy file tree
	my $retries = 5;
	my $fail_delay = 5;
	my $i = 0;
	while (1)
	{
		$i++;
		eval 
		{
			system ("scp -r $src $dest 2>> $log") == 0 or 
				die ("Could not copy $src, with error code " , $? >> 8, "\n");		
		};
		unless ($@) { last; } # break if success
		if ($i == $retries)
		{
			die ("Upload of javadoc failed after $i attempts\n");
		}
		print "scp failed, trying again...\n";
		sleep ($fail_delay);
	}
	
}


##################
#  main
##################

# in the eval below, each step is done consecutively until 
# one step dies, which causes a break out of the eval statement.
eval
{	
	# create a lock
	if (-e $fnLock)
	{
		exit;
	}
	else
	{
		system ("touch $fnLock") == 0 or die;
	}

	my %repos = (
		PV => {
				repo => "http://svn.bigcat.unimaas.nl/pathvisio/trunk",
				wc => "$dir/pathvisio",
				log => "$dir/pv_svnerr.txt",
				stamp => "$basedir/pv_curr_revision"
			},
		WP => {
				repo => "http://svn.bigcat.unimaas.nl/wikipathways/trunk",
				wc => "$dir/wikipathways",
				log => "$dir/wp_svnerr.txt",
				stamp => "$basedir/wp_curr_revision",
			},
		CYTOSCAPE26 => {
				repo => "http://chianti.ucsd.edu/svn/cytoscape/trunk",
				wc => "$dir/cytoscape2.6",
				log => "$dir/cytoscape26_svnerr.txt",
				stamp => "$basedir/cytoscape26_curr_revision",
			},
		PVPLUGINS => {
				repo => "http://svn.bigcat.unimaas.nl/pvplugins/trunk",
				wc => "$dir/pvplugins",
				log => "$dir/pvplugins_svnerr.txt",
				stamp => "$basedir/pvplugins_curr_revision",
			},
		BRIDGEDB => {
				repo => "http://svn.bigcat.unimaas.nl/bridgedb/trunk",
				wc => "$dir/bridgedb",
				log => "$dir/bridgedb_svnerr.txt",
				stamp => "$basedir/bridgedb_curr_revision",
			},
	);

	my $cytoscapedir= $repos{CYTOSCAPE26}->{wc};
	my $pathvisiodir= $repos{PV}->{wc};

	my $anyNewer = 0;
	for my $key (keys %repos)
	{
		if (check_revision ($repos{$key}))
		{
			print $key . " is newer\n";
			$anyNewer = 1;
			
			do_step ( 
				name => "$key SVN CHECKOUT",
				log => $repos{$key}->{log},
				action => sub
				{
					checkout_or_update ($repos{$key});
				}
			);
		}
	}

	if (!$anyNewer)
	{
		exit; # nothing to do
	}

	print "Location: $dir\n";

	if ($repos{CYTOSCAPE26}->{newer})
	{
		write_stamp($repos{CYTOSCAPE26});

		my $subdir = $repos{CYTOSCAPE26}->{wc};
		chdir ("$subdir");
		
		# Next step: compile the main project
		do_command_step (
			name => "CYTOSCAPE COMPILE",
			log => "$subdir/compile1.txt",
			cmd => "ant",
		);
	}

	if ($repos{BRIGDEDB}->{newer})
	{
		write_stamp($repos{BRIDGEDB});
		
		my $subdir = $repos{BRIDGEDB}->{wc};
		chdir ("$subdir/corelib");
		
		# Next step: compile the main project
		do_command_step (
			name => "BRIDGEDB COMPILE",
			log => "$subdir/compile1.txt",
			cmd => "ant",
		);

		# Next step: compile the main project
		do_command_step (
			name => "BRIDGEDB TEST",
			log => "$subdir/junit.txt",
			cmd => "ant test",
		);

		# Next step: create javadocs and upload them to the web
		do_step (
			name => "BRIDGEDB DAILY ONLINE JAVADOCS",
			log => "$subdir/docs.txt",
			action => sub
			{
				#generate docs
				system ("ant doc > $subdir/docs.txt") == 0 or 
					die ("docs failed with error code ", $? >> 8, "\n");
				
				scp_dir (
					src => "$subdir/corelib/doc/*",
					dest => "pathvisio\@www.pathvisio.org:/home/pathvisio/apidoc/bridgedb",
					log => "$subdir/docs.txt",
				);
			}
		);
		
		# copy bridgedb.jar from bridgedb repository to pathvisio repository
		do_command_step (
			name => "COPY BRIDGEDB",
			log => undef,
			cmd => "cp " . $repos{BRIDGEDB}->{wc} . "/corelib/bridgedb.jar " . $repos{PV}->{wc} . "/lib"
		);
	}
	
	if ($repos{PV}->{newer} || $repos{CYTOSCAPE26}->{newer} || $repos{BRIDGEDB}->{newer})
	{
		write_stamp($repos{PV});

		my $subdir = $repos{PV}->{wc};
		chdir ($subdir);
		
		# Next step: compile the main project
		do_command_step (
			name => "PATHVISIO COMPILE ALL",
			log => "$subdir/compile1.txt",
			cmd => "ant all",
		);

		# Next step: compile the webservice client library
		do_command_step (
			name => "PATHVISIO COMPILE WEBSERVICE CLIENT LIB",
			log => "$dir/compile-wpc.txt",
			cmd => "ant jar-wpclient " . 
				"-Dwsdl.url=http://137.120.14.24/wikipathways-test/wpi/webservice/webservice.php?wsdl",
		);

		# Next step: do all JUnit unit tests
		do_command_step (
			name => "PATHVISIO JUNIT TEST",
			log => "$subdir/junit.txt",
			cmd => "ant test",
		);

		# Next step: create javadocs and upload them to the web
		do_step (
			name => "PATHVISIO DAILY ONLINE JAVADOCS",
			log => "$subdir/docs.txt",
			action => sub
			{
				#generate docs
				system ("ant docs > $subdir/docs.txt") == 0 or 
					die ("docs failed with error code ", $? >> 8, "\n");
					
				
				#copy docs to website				
				scp_dir (
					src => "$subdir/apidoc/*",
					dest => "pathvisio\@www.pathvisio.org:/home/pathvisio/apidoc",
					log => "$subdir/docs.txt",
				);
			}
		);

		# Next step: create a webstart and upload that to the web as well.
		do_step (
			name => "DAILY WEBSTART",
			log => "$subdir/webstart.txt",
			action => sub
			{
				system ("ant prepare-webstart > $subdir/webstart.txt") == 0 or 
					die ("prepare-webstart failed with error code ", $? >> 8, "\n");
				
				# substitute webstart codebase url
				
				system ("sed -i 's#codebase=\"http://www.pathvisio.org/webstart\"#codebase=\"http://www.pathvisio.org/webstart/daily\"#' $subdir/webstart/www/*.jnlp") == 0 or
					die ("Couldn't substitute codebase url, with error code ", $? >> 8, "\n");
					
				# copy files to website
				scp_dir (
					src => "$subdir/webstart/www/*",
					dest => "pathvisio\@www.pathvisio.org:/home/pathvisio/webstart/daily",
					log => "$subdir/webstart.txt",
				);
				
			}
		);

		# Next step: test gpmldiff shell scripts
		# if this fails, it means that gpmldiff.sh can't be run.
		do_step (
			name => "GPMLDIFF SHELL TEST",
			log => undef,
			action => sub
			{
				my $fnOut1 = "test.result1.txt";
				my $fnOut2 = "test.result2.txt";
				my $fnIn1 = "tools/gpmldiff/testcases/Simple1.1.gpml";
				my $fnIn2 = "tools/gpmldiff/testcases/Simple1.2.gpml";

				# test gpmldiff -o table option
				system ("sh gpmldiff.sh -o table $fnIn1 $fnIn2 > $fnOut1") == 0
					or die "gpmldiff -o table failed, $?";

				# test gpmldiff -o dgpml option
				system ("sh gpmldiff.sh -o dgpml $fnIn1 $fnIn2 > $fnOut2") == 0
					or die "gpmldiff -o dgpml failed, $?";
			}
		);

		# Next step: check that all source files contain a license header.
		# We tend to forget adding this.
		do_step (
			name => "LICENSE HEADER CHECK",
			log => "$dir/license_check.txt",
			action => sub
			{
				# check if all java files have the Apache License attached
				system (qq^find . -wholename "**/org/pathvisio/**/*.java" ! -name "Revision.java" ! -wholename "**/src-axis-wpclient/**" | xargs -d '\n' grep -L "Apache License, Version 2.0" > $dir/license_check.txt^) == 0 or
					die ("find command failed with error code ", $? >> 8, "\n");
				
				# check that the number of lines in the output is equal to 0
				open INFILE, "$dir/license_check.txt" or die "License check output missing\n";
				my @files = <INFILE>;
				close INFILE;
				if (@files > 0) { die ("License header missing on some files\n"); }
			}
		);
		
		# test compilation of kegg converter
		do_command_step (
			name => "CYTOSCAPE-GPML",
			log => "$subdir/cytoscape-gpml.txt",
			cmd => 'ant -f tools/cytoscape-gpml/build.xml '.
					"-Dcytoscape.dir=$cytoscapedir " .
					'-Dwsdl.url=http://www.wikipathways.org/wpi/webservice/webservice.php?wsdl',
		);
		
		# Next step: check that java files have svn propset svn:eol-style native
		# Note: disabled because git-svn doesn't support propset atm.
		#~ do_step (
			#~ name => "SVN:EOL-STYLE PROPERTY",
			#~ log => "$dir/props.txt",
			#~ action => sub
			#~ {
				#~ our @javalist;
				#~ sub wanted { if (-f $_ && /\.java$/i && ! (/Revision.java$/)) { push @javalist, $File::Find::name; } }
				#~ find (\&wanted, "$dir/src");

				#~ system ("touch $dir/props.txt") == 0 or die ("Can't touch. Look ma, no hands? $!");
				#~ open OUTPUT, ">$dir/props.txt" or die $!;
				#~ my $cWrong = 0;
				
				#~ for my $file (@javalist)
				#~ {
					#~ if (`svn propget svn:eol-style $file` !~ /native/)
					#~ {
						#~ print OUTPUT $file, "\n";
						#~ $cWrong++;
					#~ }
				#~ }
				
				#~ close OUTPUT;
				
				#~ if ($cWrong > 0)
				#~ {
					#~ die "$cWrong java files are missing the svn:eolstyle property";
				#~ }
			#~ }
		#~ );

		# Next step: checkstyle
		do_step (
			name => "CHECKSTYLE",
			log => "$subdir/cs_result.txt",
			action => sub
			{
				system ("ant checkstyle") == 0 or 
					die ("ant [checkstyle] failed with error code ", $? >> 8, "\n");
					
				#Now do a bit of magic so we only report NEW errors.
				my $cNew = 0;
				
				system ("touch $fnCheckstyle") == 0 or die ("Can't touch. Look ma, no hands? $!");
				open OLD, "$fnCheckstyle" or die $!;
				
				# create a hash of all old warnings
				# filter out the path before /src/ as it's different each run
				my %lOld =  map { $_ =~ s#^.*/src/#src/#; $_ => 1 } <OLD>;
				close OLD;
				
				open OUTPUT, ">$subdir/cs_result.txt" or die $!;
				print OUTPUT "New warnings:\n";
				
				open NEW, "$subdir/warnings.txt" or die $!;			
				while (my $line = <NEW>)
				{
					# filter out the path before /src/ as it's different each run
					$line =~ s#^.*/src/#src/#;
					if (!exists $lOld{$line})
					{
						$cNew++;
						print OUTPUT $line;
					}
				}
				close NEW;
				close OUTPUT;
				
				system ("mv $subdir/warnings.txt $fnCheckstyle" ) == 0 or 
					die ("mv [checkstyle] failed with error code ", $? >> 8, "\n");
				
				# here is the logic bit: we bail out if there are any NEW warnings.
				if ($cNew > 0) { die "$cNew new checkstyle warnings" };
			}
		);
		
		do_command_step (
			name => "METRICS",
			log => undef,
			cmd => "cd tools/dailybuild && ./metric.sh",
		);

		# test compilation of kegg converter
		do_command_step (
			name => "KEGG CONVERTER",
			log => "$subdir/keggconverter.txt",
			cmd => "ant -f tools/KeggConverter/build.xml",
		);

	}
	
	if ($repos{PV}->{newer} || $repos{PVPLUGINS}->{newer} || $repos{BRIDGEDB}->{newer})
	{
		write_stamp($repos{PVPLUGINS});

		my $subdir = $repos{PVPLUGINS}->{wc};
		chdir ("$subdir");
		
		# test compilation of ppp
		do_command_step (
			name => "PVPLUGINS PPP COMPILE",
			log => "$subdir/compile_ppp.txt",
			cmd => "ant -f ppp/build.xml -Dpathvisio.dir=$pathvisiodir",
		);

		# test compilation of MappBUILDER
		do_command_step (
			name => "PVPLUGINS MAPPBUILDER COMPILE",
			log => "$subdir/compile_mappbuilder.txt",
			cmd => "ant -f MAPPBuilder/build.xml -Dpathvisio.dir=$pathvisiodir",
		);
	}

	if ($repos{WP}->{newer})
	{
		write_stamp($repos{WP});

		my $subdir = $repos{WP}->{wc};
		chdir ($subdir);
		
		# Add tests here...
	}

};

# send email in case of failures
# send a general error message, 
# plus the log of the step that failed, 
# plus the last 20 entries in the subversion log (so you can see who did it :) )

#if ($@)
{
	my $msg;
	my $subject;
	if ($@) 
	{
		$subject = "[BUILD] error during " . $steps[-1]{name};
		$msg = "Error during " . $steps[-1]{name} . ":\n" . $@;

		$msg .= "

An error occurred during the daily test procedure of PathVisio.  Below
you should find a log of the step that failed, plus the last 20 commit
messages. Please check if you checked in code that caused this error
and try to fix if at all possible.
";

	}
	else
	{
		$subject = "[BUILD] succes!";
		$msg = "Succes!";
	}
	
	# amend with svn log and revision
	my $svnversion = `svnversion`;
	die "svnversion failed: $?" if $?;
	system ("svn log --limit 20 > $dir/svnlog.txt");
	
	# read output from failed step
	my @lines;
	my $log = $steps[-1]{log};
	if (defined $log)
	{
		open IN, "$log" or die "Couldn't read from log file, $!";
		@lines = <IN>;
		close IN;
	}

	open IN, "$dir/svnlog.txt" or die "Couldn't read from log file, $!";
	my @svnlog = <IN>;
	close IN;

	#compose an email
	my $out;
	if ($fSendEmail)
	{
	    open $out, "| mail @emails -s '$subject'" or die "Failed to send email, $!";
	}
	else
	{
	    $out = \*STDOUT;
	}
	
	print $out "This is an autogenerated email, please do not reply.

SVN revision: $svnversion

The build system reported: $msg
";
	print $steps[-1]{error}, "\n\n";
	
	print $out "\n----------------\n\n";
	print $out @lines;	
	print $out "\n----------------\n\n";	
	print $out "Here is the latest subversion log:\n";
	print $out @svnlog;
	
	close $out;
}

END 
{
	# remove lock file
	unlink $fnLock;
	print "Lock file removed";

}
