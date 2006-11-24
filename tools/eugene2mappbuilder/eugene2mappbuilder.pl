#!/usr/bin/perl

use strict;
use warnings;

use ListData;
use File::Find;

=pod

=head1 NAME

Eugene to mappbuilder converter, proof-of-concept

=head1 DESCRIPTION

This will convert all *.pwf files in the current
directory and all subdirectories into 
tab delimited text files with corresponding names,
that can be opened by MappBuilder.

The actual work is done by ListData.pm. The ListData package is set 
up in such a way that in
the future it will be easy to add other output options, 
such as .gpml or .mapp.

=cut

############
#   main   #
############

my @fnList;

sub wanted 
{
	if (/\.pwf$/)
	{
		push @fnList, $File::Find::name;
	}
}

# search current directory and all subdirectories for pwf files
find (\&wanted, ".");

for my $fnIn (@fnList)
{
	my $fnOut = $fnIn; 
	if ($fnOut =~ /\.pwf$/) { $fnOut =~ s/\.pwf$/.txt/; } 
	else { $fnOut .= ".txt"; }
	
	print "$fnIn -> $fnOut\n";
	my $data = ListData::read_from_pwf($fnIn);
	$data->write_to_txt($fnOut);
};