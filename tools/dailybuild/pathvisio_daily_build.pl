#!/usr/bin/perl
#
# Simple Daily build script
#
# - make a fresh checkout of pathvisio-trunk in a temp dir
# - compile v1 and v2, check for errors
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

###############
#   globals
###############

use File::Temp qw / tempfile tempdir /;

my $dir = tempdir ( CLEANUP => 1 );
#~ my $dir = "/home/martijn/temp";

# these people will be emailed when a problem occurs
my @emails = (
    'martijn.vaniersel@bigcat.unimaas.nl', 
#    'wikipathways-commit@googlegroups.com',    
#    'thomas.kelder@bigcat.unimaas.nl',
    );

print "Location: $dir\n";

my @steps;

# if fSendEmail is false, the email will not be sent but
# displayed on the screen

my $fSendEmail = 1;

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

##################
#  main
##################

# in the eval below, each step is done consecutively until 
# one step dies, which causes a break out of the eval statement.
eval
{
	# create a temp dir and do a fresh checkout
	chdir ($dir);

	# First step: do a fresh checkout
	do_step ( 
		name => "SVN CHECKOUT",
		log => "$dir/svnerr.txt",
		action => sub
		{
			# For some reason, our svn server sometimes fails on the first attempt but then works fine afterwards.
			# So if it fails, retry a few times
			my $retries = 5;
			my $fail_delay = 5;
			my $i = 0;
			unlink "$dir/svnerr.txt";
			while (1)
			{
				$i++;

				eval {
					system ("svn checkout http://svn.bigcat.unimaas.nl/pathvisio/trunk . 2>>$dir/svnerr.txt") == 0
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
	);

	# Next step: test the compile-v1 ant target
	do_step (
		name => "COMPILE ALL",
		log => "$dir/compile1.txt",
		action => sub
		{
			# compile
			system ("ant all > $dir/compile1.txt") == 0 or 
				die ("compile all failed with error code ", $? >> 8, "\n");
		}
	);

	# Next step: do all JUnit unit tests
	do_step (
		name => "JUNIT TEST",
		log => "$dir/junit.txt",
		action => sub
		{
			system ("ant test > $dir/junit.txt") == 0 or 
				die ("test failed with error code ", $? >> 8, "\n");
		}
	);

	# Next step: ogretest (test dependencies of core)
	do_step (
		name => "OGRETEST",
		log => "$dir/ogretest.txt",
		action => sub
		{
			# compile
			system ("ant ogretest > $dir/ogretest.txt") == 0 or 
				die ("compile all failed with error code ", $? >> 8, "\n");
		}
	);


	# Next step: create javadocs and upload them to the web
	do_step (
		name => "DAILY ONLINE JAVADOCS",
		log => "$dir/docs.txt",
		action => sub
		{
			#generate docs
			system ("ant docs > $dir/docs.txt") == 0 or 
				die ("docs failed with error code ", $? >> 8, "\n");
				
			#copy docs to website				
			my $retries = 5;
			my $fail_delay = 5;
			my $i = 0;
			while (1)
			{
				$i++;
				eval 
				{
					system ("scp -r $dir/apidoc/* martijn.vaniersel\@ftp2:/home/martijn.vaniersel/public_html/pathvisio/daily/javadoc 2>> $dir/docs.txt") == 0 or 
						die ("Could not copy javadocs, with error code " , $? >> 8, "\n");		
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
	);

	# Next step: create a webstart and upload that to the web as well.
	do_step (
		name => "DAILY WEBSTART",
		log => "$dir/webstart.txt",
		action => sub
		{
			system ("ant prepare-webstart > $dir/webstart.txt") == 0 or 
				die ("prepare-webstart failed with error code ", $? >> 8, "\n");
			
			# substitute webstart codebase url
			
			system ("sed -i 's#codebase=\"http://blog.bigcat.unimaas.nl/~gmmlvisio\"#codebase=\"http://ftp2.bigcat.unimaas.nl/~martijn.vaniersel/pathvisio/daily/webstart\"#' $dir/webstart/www/*.jnlp") == 0 or
				die ("Couldn't substitute codebase url, with error code ", $? >> 8, "\n");
				
			# copy files to website
			
			my $retries = 5;
			my $fail_delay = 5;
			my $i = 0;
			while (1)
			{
				$i++;
				eval 
				{
					system ("scp -r $dir/webstart/www/* martijn.vaniersel\@ftp2:/home/martijn.vaniersel/public_html/pathvisio/daily/webstart") == 0 or 				
						die ("Could not copy webstart, with error code " , $? >> 8, "\n");		
				};
				unless ($@) { last; } # break if success
				if ($i == $retries)
				{
					die ("Upload of webstart files failed after $i attempts\n");
				}
				print "scp failed, trying again...\n";
				sleep ($fail_delay);
			}

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
			system (qq^find . -name "*.java" ! -name "Revision.java" | xargs -d '\n' grep -l "package org.pathvisio" | xargs -d '\n' grep -L "Apache License, Version 2.0" > $dir/license_check.txt^) == 0 or
				die ("find command failed with error code ", $? >> 8, "\n");
			
			# check that the number of lines in the output is equal to 0
			open INFILE, "$dir/license_check.txt" or die "License check output missing\n";
			my @files = <INFILE>;
			close INFILE;
			if (@files > 0) { die ("License header missing on some files\n"); }
		}
	);

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
