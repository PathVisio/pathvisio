#!/usr/bin/perl

use strict;
use warnings;

use ListData;

=pod

=head1 NAME

Eugene to mappbuilder converter, proof-of-concept

=head1 DESCRIPTION

This will convert all data\*.pwf files into 
tab delimited text files with corresponding names,
that can be opened by MappBuilder.

The actual work is done by ListData.pm. The ListData package is set 
up in such a way that in
the future it will be easy to add other output options, 
such as .gmml or .mapp.

=cut

############
#   main   #
############

my @fnList = glob ("data/*.pwf");

for my $fnIn (@fnList)
{
	my $fnOut = $fnIn; 
	if ($fnOut =~ /\.pwf$/) { $fnOut =~ s/\.pwf$/.txt/; } 
	else { $fnOut .= ".txt"; }
	
	print "$fnIn -> $fnOut\n";
	my $data = ListData::read_from_pwf($fnIn);
	$data->write_to_txt($fnOut);
};