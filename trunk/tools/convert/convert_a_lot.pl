#!/usr/bin/perl
#
# program will convert old GMML files to new ones,
# and validate every step automagically.
#

use warnings;
use strict;

use XML::LibXML;
use File::Find;
use File::Spec;

use converter;

#################
#    config     #
#################

my $dirMapps = "E:/GenMAPP 2 Data/MAPPs";
my $dirGmml = "E:/Gmml-Visio Data/MAPPs";

#################
#   globals     #
#################

my $fnSchemaOld = 'e:/prg/gmml-visio-trunk/GMML_compat.xsd';
my $uriSchemaSchema = 'http://www.w3.org/2001/XMLSchema.xsd';

my $dieOnError = 0; # die on first error encountered

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
	
	system ("java", "-cp", '"lib/JRI.jar";"lib/org.eclipse.core.commands_3.2.0.I20060605-1400.jar";"lib/org.eclipse.equinox.common_3.2.0.v20060603.jar";"lib/org.eclipse.jface_3.2.0.I20060605-1400.jar";"lib/org.eclipse.swt_3.2.0.v3232o.jar";"lib/jdom.jar";build;"lib/hsqldb.jar";"lib/org.eclipse.swt.win32.win32.x86_3.2.0.v3232m.jar"', "util.Converter", $fnMapp, $fnOut);
	print "Exit status ", $? >> 8, "\n";
	if ($?)
	{
		die if ($dieOnError);
		unlink $fnOut;
	}
}

#################
#   main        #
#################

# find all mapps on computer...

my @list;
sub wanted { if (-f $_ && /\.mapp$/i && ! (/_back\.mapp$/i)) { push @list, $File::Find::name; } }
find (\&wanted, $dirMapps);

my $last;
@list = sort @list;

chdir ("../..");
my %okDirs;

#convert mapps 2 gmmlOld
for my $fnIn (@list)
{
	my $fnOut = $fnIn; 
	$fnOut =~ s/(.mapp$)/.gmml/i;
	$fnOut =~ s/$dirMapps/$dirGmml/i;

	my $targetDir = $fnOut;
	$targetDir =~ s#[^/]*$##; #remove part after last slash
	unless (exists $okDirs{$targetDir})
	{
		mkdirs ($targetDir);
		$okDirs{$targetDir} = 1;
	}
	
	convert ($fnIn, $fnOut);
}

