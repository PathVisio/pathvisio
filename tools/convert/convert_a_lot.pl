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
my $dirGmml = "E:/Gmml-Visio Data/Mapps";

#################
#   globals     #
#################

my $fnSchemaOld = 'e:/prg/gmml/trunk/xsd/GMML_compat.xsd';
my $fnSchemaNew = 'e:/prg/gmml/trunk/xsd/GMML.xsd';
my $uriSchemaSchema = 'http://www.w3.org/2001/XMLSchema.xsd';

my $dieOnError = 1; # die on first error encountered

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
	
	system ("java", "-cp", ".;\"E:\\lib\\jdom.jar\"", "Converter", $fnMapp, $fnOut);
	print "Exit status ", $? >> 8, "\n";
	die if ($dieOnError && $?);
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

chdir ("../mapp2gmml/src");
my %okDirs;

#convert mapps 2 gmmlOld
for my $fnIn (@list)
{
	my $fnOut = $fnIn; 
	$fnOut =~ s/(.mapp$)/.xml/i;
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

