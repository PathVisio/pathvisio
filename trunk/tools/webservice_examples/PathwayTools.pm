# PathwayTools.pm
# Copyright (c) 2008, BiGCaT Bioinformatics

use strict;
use warnings;

package PathwayTools;

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

1;
