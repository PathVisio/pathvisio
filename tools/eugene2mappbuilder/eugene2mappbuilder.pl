#!/usr/bin/perl
#
# PathVisio,
# a tool for data visualization and analysis using Biological Pathways
# Copyright 2006-2007 BiGCaT Bioinformatics
#
# Licensed under the Apache License, Version 2.0 (the "License"); 
# you may not use this file except in compliance with the License. 
# You may obtain a copy of the License at 
# 
# http://www.apache.org/licenses/LICENSE-2.0 
#  
# Unless required by applicable law or agreed to in writing, software 
# distributed under the License is distributed on an "AS IS" BASIS, 
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
# See the License for the specific language governing permissions and 
# limitations under the License.
#

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