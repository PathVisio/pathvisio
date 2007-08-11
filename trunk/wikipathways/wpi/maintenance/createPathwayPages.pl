#!/usr/bin/perl

# This script creates a pathway page for all pathway images on the wiki
#
# Usage:
# perl createPathwayPages.pl [overwrite]
#
# Including the overwrite option will cause all existing pages to be overwritten
#
# you need to create a file c:\local\etc\wiki.cfg
# containing 2 lines:
# user=USERNAME
# pass=PASSWORD
#
# - Thomas

use strict;
use warnings;

use WWW::Mechanize;
use IO::File;

my $overwrite = 0;
if(@ARGV) {
	$overwrite = shift @ARGV eq "overwrite";
}

print "> Existing pages will " . ($overwrite ? "" : "not ") . "be overwritten\n";

# The url of the wiki
my $wiki_url = "http://blog.bigcat.unimaas.nl/pathwaywiki";

# The organism codes from the pathway files
my %organisms = (
	Hs => "Human",
	Sc => "Yeast",
	Mm => "Mouse",
	Rn => "Rat",
	);

# mechanize object for wiki
my $mech = WWW::Mechanize->new();

# point to the wiki
$mech->get ("$wiki_url/index.php?title=Special:Userlogin");

#login
my $config= readconfig();
$mech->submit_form(
        form_name => 'userlogin',
        fields      => 
			{
				wpName    	=> $config->{user},
				wpPassword  => $config->{pass},
			}
    );

# read images Special:Allpages from the old wiki
$mech->get ("$wiki_url/index.php?title=Special%3AAllpages&from=&namespace=6");

$mech->content =~ /<!-- start content -->(.*)<!-- end content -->/s;
my $allpages = $1;

# extract all image url's
while ($allpages =~ /<td><a href=\"(.*?)\" title=\"(.*?)\">(.*?)<\/a><\/td>/gs) {
	print "Processing image: $2\n";
	
	my $title = $2;
	
	my $code = getSpeciesCode($title);
	if(defined $code) {
		my $page = imgpage2pwpage($title, $code);
		if(defined $page) {
   		print "Creating page $page\n";
   			
   		my $target_url = $wiki_url . "/index.php?title=$page&action=edit";
   		$mech->get ($target_url);
   		
   		$page =~ m/^[\w|\s]*:([\w|\s]*)/;
   		my $category = "Category:$organisms{$code}|$1";
   		# grab the contents from the textarea
   		$mech->content =~ /<textarea .*?>(.*?)<\/textarea>/gs;
   		my $page_content = $1;
   		
   		chomp($page_content);
   		if($page_content eq "" || $overwrite) {
   			# submit the new content
   			$mech->submit_form(
   				form_name => 'editform',
   				fields => {
   					wpTextbox1 => "[[$title]]\n[[$category]]"
   				}
   			);
   		} else {
   			print("\t!Page already exists!\n");
   		}
   	}
	}
}

sub imgpage2pwpage {
	my $imgpage = shift;
	my $code = shift;
	
	my $species = $organisms{$code};
  if(defined $species) {
	  	 $imgpage =~ s/Image:$code /$species:/;
	  	 $imgpage =~ s/\.\w*$//;
	  	 return $imgpage;
	  } else {
	  	print "Could not find organism for image $imgpage\n"; 
		}
	undef;
}

sub getSpeciesCode {
	my $imgpage = shift;
	
	if ($imgpage =~ /^Image:([A-Z][a-z]) /) {
	  return $1;
	}
	print "Could not find organism code for image $imgpage\n";
	undef;
}

sub readconfig {
    my %params;
    my $fh= IO::File->new("wiki.cfg", "r") or die "wiki.cfg: $!";
    while (<$fh>) {
        s/\s+$//;
        if (/(\w+)\s*=\s*(.*)/) {
            my ($k, $v)= ($1, $2);
            $params{$k}= $v;
        }
    }
    $fh->close();
    return \%params;
}