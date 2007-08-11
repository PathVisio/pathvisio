#!/usr/bin/perl

#
# This is a quick and dirty script that I wrote to copy the
# textual contents of one mediawiki to another using the WWW::Mechanize
# perl module.
#
# I've added it here because it is a demonstration of how to update
# mediawiki through a perl script.
#
# This script doesn't copy images or other uploads, only text.
# 

use WWW::Mechanize;
 
# one mechanize object to read from
my $mech = WWW::Mechanize->new();

# one mechanize object to write to
my $mechout = WWW::Mechanize->new();

# point the former to the old wiki (login page)
$mech->get ("http://blog.bigcat.unimaas.nl/wiki/index.php?title=Special:Userlogin");

# point the latter to the new wiki (login page)
$mechout->get ("http://wiki.bigcat.unimaas.nl/index.php?title=Special:Userlogin");

# log in
$mech->submit_form(
        form_name => 'userlogin',
        fields      => 
			{
				wpName    	=> "Martijn",
				wpPassword  => "***",
			}
    );

$mechout->submit_form(
        form_name => 'userlogin',
        fields      => 
			{
				wpName    	=> "Martijn",
				wpPassword  => "***",
			}
    );

# read Special:Allpages from the old wiki
$mech->get ("http://blog.bigcat.unimaas.nl/wiki/index.php/Special:Allpages");

$mech->content =~ /<!-- start content -->(.*)<!-- end content -->/s;
my $allpages = $1;

# extract all url's
while ($allpages =~ /<td><a href=\"(.*?)\" title=\"(.*?)\">(.*?)<\/a><\/td>/gs)
{
	# for each url, 
	my $url = "http://blog.bigcat.unimaas.nl" . $1;
	my $title = $2;
	
	# download it
	$mech->get ($url);
	
	# simulate a click on "edit"
	$mech->follow_link ( text => "Edit" );

#~ print "-" x 40, "\n";
#~ print $mech->content;
#~ print "-" x 40, "\n";
	
	# grab the contents from the textarea
	$mech->content =~ /<textarea .*?>(.*?)<\/textarea>/gs;
	my $wiki_content = $1;

	# create a new page on the target in edit mode:
	my $target_url = $url; 
	$target_url =~ s#http://blog.bigcat.unimaas.nl/wiki/index.php/(.*)#http://wiki.bigcat.unimaas.nl/index.php?title=$1&action=edit#;
	$mechout->get ($target_url);
	
	# html entity encoding. 
	# This is incomplete but good enough for the original purpose of the script
	$wiki_content =~ s/&lt;/</sg;
	$wiki_content =~ s/&gt;/>/sg;
	$wiki_content =~ s/&quot;/"/sg;

	# submit the new content
	$mechout->submit_form 
	(
		form_name => 'editform',
		fields => {
			wpTextbox1 => $wiki_content
		}
	);
	
	print "Done: $title\n";
		
}

#~ $mech->get ("http://blog.bigcat.unimaas.nl/wiki/index.php?title=Hsqldb_database_schemas&action=edit");


# wait for enter key
<STDIN>;
