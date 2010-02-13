#!/usr/bin/perl

#use warnings;
use strict;

use PathwayTools::Pathway;
use PathwayTools::PathwayElement;
use PathwayTools;
use SOAP::Lite;
use HashSpeciesList;

#
# Script to convert single target pathway at WikiPathways from a source file.
# User enters WP identifiers for from and to pathways
# Necessary input files: 
# Homology information from Homologene for relevant species conversion
# Homology information from BioMART for relevant species conversion
# Gene symbol information for source and target species from BioMART
# Output:
# Updates (overwrites) target pathway at WP 

#####################

#Define log files
#Tracks IDs that didn't convert and percentage conversion per pathway.
my $outfilename1 = "Log-HomologyConvert-Descriptions.txt";	
unless ( open(LOGFILE1, ">$outfilename1") )
       {
         print "could not open file $outfilename1\n";
         exit;
 	}
print LOGFILE1 "Source\t\tTarget\n";

####################

my $day = (localtime(time))[3];
my $month = 1+(localtime(time))[4];
my $year = 1900+(localtime(time))[5];
my $date = "$month/$day/$year";
my $cutoff = "20070522222100";
my $maintbot = "MaintBot";
my $fnGPML = "GPML.xsd";

# Use this flag for testing the script on the TEST site
my $testing = 0;

## Under script control
## Usage: ./HomologyConvert-single.pl -t <password> <from WPID> <to WPID> (-t is optional for testing mode)
my $scriptmode = 0;
my $id1 = 0;
my $id2 = 0;
my $password = 0;

if ($#ARGV == 3) { # with testing flag
        $testing = 1; # true!
        $password = $ARGV[1];
        $id1 = $ARGV[2];
        $id2 = $ARGV[3];

        #trigger script mode
        $scriptmode = 1; # true!
} elsif ($#ARGV == 2) { # without testing flag
        $password = $ARGV[0];
        $id1 = $ARGV[1];
        $id2 = $ARGV[2];

        #trigger script mode
        $scriptmode = 1; #true!
}

if (!$scriptmode) {
	#Ask user for pathway ID to convert FROM
	print "\nEnter the wikipathways ID of the pathway you want to convert FROM (for example WP56) : ";
	$id1 = <STDIN>;
	chomp ($id1);

	#Ask user for pathway ID to convert TO
	print "\nEnter the wikipathways ID of the pathway you want to convert TO (the pathway will be overwritten) : ";
	$id2 = <STDIN>;
	chomp ($id2);

	#Ask user for WP login password
	print "\nEnter WikiPathways password for user $maintbot: ";
	$password = <STDIN>;
	chomp ($password);
}

# process ids
my $sourceid = ();
my $targetid = ();

if ($id1 =~ /^WP/)
        {
        $sourceid = $id1;
        }
elsif ($id1 =~ /^\d/)
        {
        $sourceid = "WP".$id1;
        }

if ($id2 =~ /^WP/)
        {
        $targetid = $id2;
        }
elsif ($id2 =~ /^\d/)
        {
        $targetid = "WP".$id2;
        }

######################

#Create service interface with fault handler and login
my $wp_soap;
my $auth;

if ($testing) {
$wp_soap = SOAP::Lite
        ->proxy('http://137.120.14.24/wikipathways-test/wpi/webservice/webservice.php')
        ->uri('http://www.wikipathways.org/webservice')
        ->on_fault(sub {
                	my $soap = shift;
                	my $res = shift;
                	# Map faults to exceptions
                	if(ref($res) eq '') { die($res);} 
                	else {die($res->faultstring);}
                return new SOAP::SOM;
           			} );

my $name = SOAP::Data->name(name => "MaintBot"); 
my $pass = SOAP::Data->name(pass => $password);
my $wpauth = $wp_soap->login($name, $pass)->result;
$auth = SOAP::Data->name('auth' => \SOAP::Data->value(SOAP::Data->name('user' => 'MaintBot'), SOAP::Data->name('key', $wpauth)));		
print "Working with TEST site\n";
} else {
$wp_soap = SOAP::Lite
        ->proxy('http://www.wikipathways.org/wpi/webservice/webservice.php')
        ->uri('http://www.wikipathways.org/webservice')
        ->on_fault(sub {
                        my $soap = shift;
                        my $res = shift;
                        if(ref($res) eq '') { die($res);}
                        else {die($res->faultstring);}
                return new SOAP::SOM;
                                } );
my $name = SOAP::Data->name(name => "MaintBot");
my $pass = SOAP::Data->name(pass => $password);
my $wpauth = $wp_soap->login($name, $pass)->result;
$auth = SOAP::Data->name('auth' => \SOAP::Data->value(SOAP::Data->name('user' => 'MaintBot'), SOAP::Data->name('key', $wpauth)));
print "Working with LIVE site\n";
}

######################

#Get the pathway info for source and target

print "Requesting source pathway: $sourceid\n";

my $refId = SOAP::Data->name(pwId => $sourceid);
my $rev = SOAP::Data->name(revision => '0');
my @sourceResults = $wp_soap->getPathway($refId, $rev)->paramsout;	
unshift(@sourceResults, $wp_soap->getPathway($refId, $rev)->result);
my $pathway = new PathwayTools::Pathway();
my $sourceGpml = 0;
my $sourcerevision = 0;
my $sourcespecies = ();
my $sourcename = ();
	
foreach my $ref1 (@sourceResults)
	{
	$sourceGpml = $ref1->{gpml};
	$sourcerevision = $ref1->{revision};
	$sourcespecies = $ref1->{species};
	$sourcename = $ref1->{name};
	}

print "Requesting info for target pathway: $targetid\n";
my $refId2 = SOAP::Data->name(pwId => $targetid);
my $rev2 = SOAP::Data->name(revision => '0');
my @targetResults = $wp_soap->getPathway($refId2, $rev2)->paramsout;	
unshift(@targetResults, $wp_soap->getPathway($refId2, $rev2)->result);
my $targetGpml = 0;
my $targetspecies = ();
my $targetname = ();
my $targetrevision = 0;

foreach my $ref2 (@targetResults)
	{
	$targetGpml = $ref2->{gpml};
	$targetspecies = $ref2->{species};
	$targetname = $ref2->{name};
	$targetrevision = $ref2->{revision};
	}
$targetGpml =~ m/Pathway xmlns="(.*?)"/;
my $NS = $1;

print "$sourcespecies _ $sourcename($sourcerevision)\n$targetspecies _  $targetname($targetrevision)\n";	

my $confirm = ();

if ($scriptmode){
	$confirm = "y";
}
while (!(($confirm =~ /y/) || ($confirm =~ /n/)))
	{
	print "Ready to overwrite the description on $targetspecies pathway $targetname with that from $sourcespecies pathway $sourcename\n";
	print "Continue? (y/n):";	
	$confirm = <STDIN>;
	chomp $confirm;
	}

if ($confirm =~ m/y/)
	{
	print "OK, continuing....\n";
	}
elsif ($confirm =~ m/n/)
	{
	die;
	}

######################

my $description = " "; #the description field to be copied over

$pathway->from_string($sourceGpml);
my $root = $pathway->{document}->getDocumentElement();
my $pwname = $root->getAttribute("Name");

        if ($root->getChildrenByTagName("Comment"))
        {
        for my $comment ($root->getChildrenByTagName("Comment"))
                {
                        my $commentSource = $comment->getAttribute("Source");
                        if ($commentSource eq "WikiPathways-description"){
                                $description = $comment->textContent;
                        }
                }
        }	

$pathway->from_string($targetGpml);
$root = $pathway->{document}->getDocumentElement();
$pwname = $root->getAttribute("Name");
$root->setAttribute("Last-Modified", $date);

# Create a comment
my $comment = $root->addNewChild ($NS, "Comment");
# source attribute can be anything you want, for example "HomologyConvert"
$comment->setAttribute ("Source", "WikiPathways-description");
# use appendText to set the text value of the comment.
$comment->appendText ($description);
		
		
#Upload file to WikiPathways and save to local files
my $edit = SOAP::Data->name(description => "Added description");
my $newgpml = $pathway->to_string();
my $gpmlcode = SOAP::Data->name(gpml => $newgpml);
my $baserevision = SOAP::Data->name(revision => $targetrevision);
$wp_soap->updatePathway($refId2, $edit, $gpmlcode, $baserevision, $auth);

#Update curation tags
my $tagname = SOAP::Data->name(tagName => "Curation:MissingDescription");
$wp_soap->removeCurationTag($refId2, $tagname, $auth);


print "$targetname updated at WikiPathways\n";
print LOGFILE1 "$sourceid\t->\t$targetid";

print "\n\nDone.\n";	
close LOGFILE1;

