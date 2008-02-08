#!/usr/bin/perl

use warnings;
use strict;
use Frontier::Client;
use Data::Dumper;
use MIME::Base64;
use XML::LibXML;

############
#   subs   #
############

# read username, password and server from a file
sub read_config()
{
	my $fnDetails = glob ("~/.wikipathways-id");
	my $user = undef;
	my $pass = undef;
	my $server = undef;

	open INFILE, $fnDetails or die "Couldn't read from $fnDetails, $!\n";

	while (my $line = <INFILE>)
	{
		if ($line =~ /^(#.*|\s*)$/)
		{
			# empty or comment, ignore
			next;
		}
		if ($line =~ /^\s*(username|password|server)\s*=\s*(.+)\s*$/)
		{
			my $key = $1;
			my $value = $2;
			
			if ($key eq "username")
			{
				$user = $value;
			}
			elsif ($key eq "password")
			{
				$pass = $value;
			}
			elsif ($key eq "server")
			{
				$server = $value;
			}
			else
			{
				die "Syntax error in config file";
			}
		}
		else
		{
			die "Syntax error in config file";
		}
	}
	close INFILE;
	
	if (!defined $user || !defined $pass || !defined $server)
	{
		die "One or more required properties were not defined in the configuration file";
	}
	
	return {user => $user, pass => $pass, server => $server};
}

############
#   main   #
############

my $login = read_config();

my $server = Frontier::Client->new('url' => $login->{server});

my $token = $server->call("WikiPathways.login", $login->{user}, $login->{pass});



sub test_adjust_pathway()
{
	my $result2 = $server->call("WikiPathways.getPathway", "RPCTEST", "Homo sapiens");

	#~ print Dumper ($result2);

	my $revision = $result2->{revision};
	my $gpml = decode_base64($result2->{gpml});

	#~ print "-" x 80, "\n", $gpml, "\n", "-" x 80, "\n";
	#~ print $revision, "\n";
	
	my $parser = XML::LibXML->new();
  
	my $doc = $parser->parse_string($gpml);
	
	my $NS = "http://genmapp.org/GPML/2007";
	
	my $root = $doc->documentElement();
	
	my $pwyGraphics = ($root->getElementsByTagName("Graphics"))[0];
	
	
	my $datanode = XML::LibXML::Element->new("DataNode");
	$datanode->setNamespace ($NS);
	$datanode->setAttribute ("TextLabel", $revision);
	$datanode->setAttribute ("Type", "Unknown");
	$datanode->setAttribute ("BackpageHead", "");
	
	my $graphics = $datanode->addNewChild ($NS, "Graphics");
	$graphics->setAttribute ("Color", "Transparent");
	$graphics->setAttribute ("CenterX", rand(10000));
	$graphics->setAttribute ("CenterY", rand(10000));
	$graphics->setAttribute ("Width", "900");
	$graphics->setAttribute ("Height", "300");
	
	my $xref = $datanode->addNewChild ($NS, "Xref");
	$xref->setAttribute ("Database", "Ensembl");
	$xref->setAttribute ("ID", "1234");
	
	$root->insertAfter ($datanode, $pwyGraphics);
	
	print "I'm going to send:\n", "-" x 40, "\n";
	print $doc->toString(2);
	print "\n", "-" x 40, "\n";
	
	my $result3 = $server->call ("WikiPathways.updatePathway", 
			"XMLRPC", 
			"Homo sapiens", 
			"webservice test", 
			$server->base64($doc->toString(2)), 
			$revision, 
			{'user' => $login->{user}, 'token' => $token}
		);
		
	print Dumper ($result3);
}


for (1..1)
{
	test_adjust_pathway();
}


