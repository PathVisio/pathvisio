#!/usr/bin/perl
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

my $dirMapps = "c:/GenMAPP 2 Data/MAPPs";
my $dirGpml = "c:/Gmml-Visio Data/MAPPs";
my $fnConf = "c:/prg/convert_test.conf";

#################
#   globals     #
#################

my $fnSchema = 'GPML.xsd';
my $uriSchemaSchema = 'http://www.w3.org/2001/XMLSchema.xsd';

my $dieOnError = 1; # die on first error encountered
my $fResume = 1; # if true, starts from entry saved in $fnConf.

#################
#    subs       #
#################

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
	system ("java", "-cp", '"lib/JRI.jar";"lib/org.eclipse.equinox.common.jar";"lib/org.eclipse.equinox.supplement.jar";"lib/org.eclipse.jface.jar";"lib/swt-win32.jar";"lib/org.eclipse.core.commands.jar";"lib/jdom.jar";build;"lib/hsqldb.jar";"lib/swt-win32-lib.jar";"lib/resources.jar"', "util.Converter", $fnMapp, $fnOut);
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


chdir ("../..");
my %okDirs;

# find all mapps on computer...

my @list;
#~ sub wanted { if (-f $_ && /\.mapp$/i && ! (/_back\.mapp$/i)) { push @list, $File::Find::name; } }
#~ find (\&wanted, $dirMapps);

@list = ("C:/GenMAPP 2 Data/MAPPs/Hs_Contributed_20060824/metabolic_process-GenMAPP/Hs_Fatty_Acid_Beta_Oxidation_1_BiGCaT.mapp",
	"C:/GenMAPP 2 Data/MAPPs/Hs_Contributed_20060824/metabolic_process-GenMAPP/Hs_Fatty_Acid_Beta_Oxidation_2_BiGCaT.mapp",
	"C:/GenMAPP 2 Data/MAPPs/Hs_Contributed_20060824/metabolic_process-GenMAPP/Hs_Fatty_Acid_Beta_Oxidation_Meta_BiGCaT.mapp",
	"C:/GenMAPP 2 Data/MAPPs/Hs_GO_Samples_20050810/Biological process/DNA repair.mapp",
	"C:/GenMAPP 2 Data/MAPPs/Hs_GO_Samples_20050810/Biological process/DNA replication.mapp",
	"C:/GenMAPP 2 Data/MAPPs/Hs_GO_Samples_20050810/Biological process/I-kappaB kinase NF-kappaB cascade.mapp",
	#~ "C:/GenMAPP 2 Data/MAPPs/Hs_KEGG_Converted_20041111/aaa.mapp",
	#~ "C:/GenMAPP 2 Data/MAPPs/Hs_KEGG_Converted_20041111/Hs_1_1_1_Trichloro_2_2_bis_4_chlorophenyl_ethane_DDT_degradation.mapp",
	#~ "C:/GenMAPP 2 Data/MAPPs/Hs_KEGG_Converted_20041111/Hs_1_4_Dichlorobenzene_degradation.mapp",
	#~ "C:/GenMAPP 2 Data/MAPPs/Hs_KEGG_Converted_20041111/Hs_2_4_Dichlorobenzoate_degradation.mapp",
	"C:/GenMAPP 2 Data/MAPPs/Mm_GO_Samples_20050810/Biological Process/DNA packaging.mapp",
	);

#~ sub wanted { if (-f $_ && /\.gpml$/i) { push @list, File::Spec->abs2rel ($File::Find::name, $dirGpmlOld); } }
#~ find (\&wanted, $dirGpmlOld);

my $last;
@list = sort @list;
if (-r $fnConf && $fResume)
{
	#read last tested from conf file.
	open INFILE, "$fnConf" or die;
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
	open OUTFILE, "> $fnConf" or die;
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
	
	convert ($fnIn, $fnOut);
	validate($fnOut);
	my $fnBack = $fnIn;
	$fnBack =~ s/(.mapp$)/_back.mapp/i;
	convert ($fnOut, $fnBack);

	#hack: mappdiff doesn't work in files with ( ) , in the filename. Just skip and hope for the best...
	unless ($fnIn =~ /[(),]/)
	{
		mappdiff ($fnIn, $fnBack);
	}
	
	unlink $fnBack; #remove back-converted file cruft.
	
	print "\n";
}
