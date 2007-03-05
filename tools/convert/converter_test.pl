#!/usr/bin/perl
#
# PathVisio,
# a tool for data visualization and analysis using Biological Pathways
# Copyright 2006-2007 BiGCaT Bioinformatics
#
# Licensed under the Apache License, Version 2.0 (the "License"); 
# you may not use this file except in compliance with the License. 
# You may obtain a copy of the License at 
# 
# http://www.apache.org/licenses/LICENSE-2.0 
#  
# Unless required by applicable law or agreed to in writing, software 
# distributed under the License is distributed on an "AS IS" BASIS, 
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
# See the License for the specific language governing permissions and 
# limitations under the License.
#
#
# program will convert old GPML files to new ones,
# and validate every step automagically.
#

#
# Please note that some part of the converter is
# sensitive to the digital symbol. Please convert
# pathways on a computer with the digital symbol set 
# to "." (american)
#

use warnings;
use strict;

use File::Find;
use File::Spec;
use converter;

#################
#    config     #
#################

my $fnDefaultConfig = "convert.conf";

my $dirMapps;
my $dirGpml;
my $fnState;
my $fnArgs;

#################
#   globals     #
#################

my $fnSchema = 'GPML.xsd';
my $uriSchemaSchema = 'http://www.w3.org/2001/XMLSchema.xsd';

my $dieOnError; # die on first error encountered
my $fResume; # if true, starts from entry saved in $fnState.

#################
#    subs       #
#################

# Read configuration file
sub read_config
{
	my $conffile = shift;
	open INFILE, "< $conffile" or die "Error: Couldn't open $conffile, $!";
	
	while (my $line = <INFILE>)
	{
		#skip emties and comments...
		if ($line =~ /^\s*$/) { next; }
		if ($line =~ /^#/) { next; }
		
		if ($line =~ /^(.*?)\s*=\s*(.*)$/)
		{
			my ($field, $value) = ($1, $2);
			if    ($field eq "mapp_in_dir")      		{ $dirMapps = $value; }
			elsif ($field eq "gpml_out_dir")          { $dirGpml = $value; }
			elsif ($field eq "state_file")         { $fnState = $value; }
			elsif ($field eq "args_file")   		{ $fnArgs = $value; }
			elsif ($field eq "die_on_error")  			{ $dieOnError = $value; }
			elsif ($field eq "resume")  			{ $fResume = $value; }
			else { die "Error: Syntax error in configuration file, near $line"; }
		}
		else
		{
			die "Error: Syntax error in configuration file, near $line";
		}
	}
	unless (-d $dirMapps && -d $dirGpml && defined $fnState && 
		defined $fnArgs && defined $dieOnError && defined $fResume)
	{	die "Error: Configuration file misses certain configuration strings!"; }
	
	close INFILE;
}

#copied from MiscUtils on CPAN
#with modifications
#use full path!
sub mkdirs ($)
{
    my $full_path = shift;    
    return if (-d $full_path);
    my $tmp_dir;
    foreach my $dir (split("/", $full_path))
    {
		$tmp_dir .= "$dir/";
		if (!-e $tmp_dir) 
		{
		    if (!mkdir($tmp_dir)) 
		    {
				return 0;
	    	}
		}
    }
    return 1;
}

#conversion test
sub convert
{
	my $fnMapp = shift;
	my $fnOut = shift;
		
	print " in: $fnMapp\n";
	print "out: $fnOut\n";
	system ("java", "-cp", 
		join (";", qw(
			"lib/JRI.jar"
			"lib/org.eclipse.equinox.common.jar"
			"lib/org.eclipse.equinox.supplement.jar"
			"lib/org.eclipse.jface.jar"
			"lib/swt-win32.jar"
			"lib/org.eclipse.core.commands.jar"
			"lib/jdom.jar"
			build
			"lib/hsqldb.jar"
			"lib/swt-win32-lib.jar"
			"lib/resources.jar")), "util.Converter", $fnMapp, $fnOut);
	print "Exit status ", $? >> 8;
	if ($?) { print " Error!"; }
	print "\n";
	die if ($dieOnError && $?);
}

sub validateSchemas 
{
	#validate XSDs
	print "Info:  Validating $fnSchema\n";
	system ("xmllint", "-noout", "-schema", $uriSchemaSchema, $fnSchema);
	
	print "Exit status ", $? >> 8;
	if ($?) { print " Error!"; }
	print "\n";
	die if ($dieOnError && $?);
}

sub validate
{
	my $fnGmml = shift;

	# validate GPML file
	system ("xmllint", "-noout", "-schema", $fnSchema, $fnGmml);
	print "Exit status ", $? >> 8;
	if ($?) { print " Error!"; }
	print "\n";
	die if ($dieOnError && $?);
}

sub mappdiff
{
	my $fn1 = shift;
	my $fn2 = shift;
	system ("tools\\mappdiff\\mappdiff.pl", $fn1, $fn2);
	print "Exit status ", $? >> 8;
	if ($?) { print " Error!"; }
	print "\n";
	die if ($dieOnError && $?);
}

#################
#   main        #
#################

read_config ($fnDefaultConfig);

chdir ("../..");
my %okDirs;

# find all mapps on computer...

my @list;
sub wanted { if (-f $_ && /\.mapp$/i && ! (/_back\.mapp$/i)) { push @list, $File::Find::name; } }
find (\&wanted, $dirMapps);

#~ @list = ("C:/GenMAPP 2 Data/MAPPs/Hs_Contributed_20060824/metabolic_process-GenMAPP/Hs_Fatty_Acid_Beta_Oxidation_1_BiGCaT.mapp",
	#~ "C:/GenMAPP 2 Data/MAPPs/Hs_Contributed_20060824/metabolic_process-GenMAPP/Hs_Fatty_Acid_Beta_Oxidation_2_BiGCaT.mapp",
	#~ "C:/GenMAPP 2 Data/MAPPs/Hs_Contributed_20060824/metabolic_process-GenMAPP/Hs_Fatty_Acid_Beta_Oxidation_Meta_BiGCaT.mapp",
	#~ "C:/GenMAPP 2 Data/MAPPs/Hs_GO_Samples_20050810/Biological process/DNA repair.mapp",
	#~ "C:/GenMAPP 2 Data/MAPPs/Hs_GO_Samples_20050810/Biological process/DNA replication.mapp",
	#~ "C:/GenMAPP 2 Data/MAPPs/Hs_GO_Samples_20050810/Biological process/I-kappaB kinase NF-kappaB cascade.mapp",
	#~ "C:/GenMAPP 2 Data/MAPPs/Hs_KEGG_Converted_20041111/aaa.mapp",
	#~ "C:/GenMAPP 2 Data/MAPPs/Hs_KEGG_Converted_20041111/Hs_1_1_1_Trichloro_2_2_bis_4_chlorophenyl_ethane_DDT_degradation.mapp",
	#~ "C:/GenMAPP 2 Data/MAPPs/Hs_KEGG_Converted_20041111/Hs_1_4_Dichlorobenzene_degradation.mapp",
	#~ "C:/GenMAPP 2 Data/MAPPs/Hs_KEGG_Converted_20041111/Hs_2_4_Dichlorobenzoate_degradation.mapp",
	#~ "C:/GenMAPP 2 Data/MAPPs/Mm_GO_Samples_20050810/Biological Process/DNA packaging.mapp",
	#~ );

#~ sub wanted { if (-f $_ && /\.gpml$/i) { push @list, File::Spec->abs2rel ($File::Find::name, $dirGpmlOld); } }
#~ find (\&wanted, $dirGpmlOld);

my $last;
@list = sort @list;
if (-r $fnState && $fResume)
{
	#read last tested from conf file.
	open INFILE, "$fnState" or die;
	$last = <INFILE>;
	chomp $last;
	close INFILE;

	#reorder list so that last tested comes first
	my @before_last = grep { ($_ cmp $last) < 0 } @list;
	my @after_last = grep { ($_ cmp $last) >= 0 } @list;
	@list = (@after_last, @before_last);
}

#~ validateSchemas();

#convert mapps 2 gmmlOld
for my $fnIn (@list)
{
	open OUTFILE, "> $fnState" or die;
	print OUTFILE $fnIn;
	close OUTFILE;

	my $fnOut = $fnIn; 
	$fnOut =~ s/(.mapp$)/.gpml/i;	
	$fnOut =~ s/$dirMapps/$dirGpml/i;

	my $targetDir = $fnOut;
	$targetDir =~ s#[^/]*$##; #remove part after last slash
	unless (exists $okDirs{$targetDir})
	{
		mkdirs ($targetDir);
		$okDirs{$targetDir} = 1;
	}
	
	#write arguments to a temporary file, to make it easier to reproduce in java
	my $ARGSFILE;
	open ($ARGSFILE, "> $fnArgs") or die "$!";
	print $ARGSFILE "\"$fnIn\"\n\"$fnOut\"";
	close $ARGSFILE;
	
	convert ($fnIn, $fnOut);
	validate($fnOut);
	my $fnBack = $fnIn;
	$fnBack =~ s/(.mapp$)/_back.mapp/i;

	open ($ARGSFILE, ">> $fnArgs") or die "$!";
	print $ARGSFILE "\n\n\"$fnOut\"\n\"$fnBack\"";
	close $ARGSFILE;

	convert ($fnOut, $fnBack);

	#hack: mappdiff doesn't work in files with ( ) , in the filename. Just skip and hope for the best...
	unless ($fnIn =~ /[(),]/)
	{
		mappdiff ($fnIn, $fnBack);
	}
	
	unlink $fnBack; #remove back-converted file cruft.
	
	print "\n";
}
