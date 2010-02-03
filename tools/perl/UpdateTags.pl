#!/usr/bin/perl

use warnings;
use strict;

use PathwayTools::Pathway;
use PathwayTools::PathwayElement;
use PathwayTools;
use SOAP::Lite;
use HashSpeciesList;
use Cwd;

#
# Script to switch between two tags on WikiPathways.
# Currently tags are hard-coded as Featured and Analysis collection.
#
# Note that script contains code for running on both test and live site, with the live site
# commented out.
#
# Output:
# WP pathways updated with new tag and with old tag removed.

#####################
my $maintbot = "MaintBot";

#Ask user for WP login password
print "\nEnter WikiPathways password for user $maintbot: ";
my $password = <STDIN>;
chomp ($password);

#####################

#Create service interface with fault handler and login

my $wp_soap_TEST = SOAP::Lite
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

my $name = SOAP::Data->name(name => $maintbot); 
my $pass = SOAP::Data->name(pass => $password);
my $wpauth = $wp_soap_TEST->login($name, $pass)->result;
my $auth_TEST = SOAP::Data->name('auth' => \SOAP::Data->value(SOAP::Data->name('user' => 'MaintBot'), SOAP::Data->name('key', $wpauth)));			

#my $wp_soap_LIVE = SOAP::Lite
#        ->proxy('http://www.wikipathways.org/wpi/webservice/webservice.php')
#        ->uri('http://www.wikipathways.org/webservice')
#        ->on_fault(sub {
#                        my $soap = shift;
#                        my $res = shift;
#                        if(ref($res) eq '') { die($res);}
#                        else {die($res->faultstring);}
#                return new SOAP::SOM;
#                                } );
#my $name2 = SOAP::Data->name(name => $maintbot);
#my $pass2 = SOAP::Data->name(pass => $password);
#my $wpauth2 = $wp_soap_LIVE->login($name2, $pass2)->result;
#my $auth_LIVE = SOAP::Data->name('auth' => \SOAP::Data->value(SOAP::Data->name('user' => 'MaintBot'), SOAP::Data->name('key', $wpauth2)));

#####################

#Get list of pathways with the specified tag and collect wpids for corresponding pathways

my $existingtag = "Curation:FeaturedPathway";
my $newtag = "Curation:AnalysisCollection";

print "Getting all pathways tagged with the $existingtag tag\n";

my $counter = 0;

my $tag1 = SOAP::Data->name(tagName => $existingtag);
my $tag2 = SOAP::Data->name(tagName => $newtag);

my @taglist = $wp_soap_TEST->getCurationTagsByName($tag1)->paramsout;
unshift(@taglist, $wp_soap_TEST->getCurationTagsByName($tag1)->result);

#my @taglist = $wp_soap_LIVE->getCurationTagsByName($tag1)->paramsout;
#unshift(@taglist, $wp_soap_LIVE->getCurationTagsByName($tag1)->result);

my %pathwayids = ();
my $id = ();
my $revision = ();

foreach my $tag (@taglist)
{
	$counter++;
	foreach my $key (keys %{$tag}) 
		{
		if ($key =~ /^pathway$/)
		{
		foreach my $pathwaykey (keys %{$tag->{$key}})
			{
			if ($pathwaykey =~ /^id$/)
				{
				$id = $tag->{$key}->{$pathwaykey};
				print "pathway id is $tag->{$key}->{$pathwaykey}\n";
				}
			elsif ($pathwaykey =~ /^revision$/)
				{
				$revision = $tag->{$key}->{$pathwaykey};
				print "revision is $tag->{$key}->{$pathwaykey}\n";
				}
            }
            $pathwayids{$id} = $revision;
		}
	}
}

print "\nNumber of collected pathways: $counter\n";

#Loop through pathway ids, add new tag

my $text = SOAP::Data->name(tagText => "");

foreach my $id (keys %pathwayids)
	{
	my $pwid = SOAP::Data->name(pwId => $id);
	my $rev = SOAP::Data->name(revision => $pathwayids{$id});
	$wp_soap_TEST->saveCurationTag($pwid, $tag2, $text, $rev, $auth_TEST); 
	$wp_soap_TEST->removeCurationTag($pwid, $tag1, $auth_TEST);
	#$wp_soap_LIVE->saveCurationTag($pwid, $tag2, $text, $rev, $auth_TEST); 
	#$wp_soap_LIVE->removeCurationTag($pwid, $tag1, $auth_TEST);
	}

print "\n\nDone.\n";	
