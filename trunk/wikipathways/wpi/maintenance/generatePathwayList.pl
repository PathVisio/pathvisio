#!/usr/bin/perl
use strict;
use warnings;
use File::Find;

my %categories = (
	'physiological_process-GenMAPP' => "Physiological Process",
	'molecular_function-GenMAPP' => "Molecular Function",
	'metabolic_process-GenMAPP' => "Metabolic Process",
	'cellular_process-GenMAPP' => "Cellular Process",
);

my $dir = shift @ARGV;
my $out = shift @ARGV;

open OUT, ">$out" or die "Can't open $out";

find(\&wanted, $dir);

my $last_dir = " ";

sub wanted {
	if(m/.mapp$/) {
		my $dir = getBottomDir($File::Find::dir);
		if($dir ne $last_dir) {
			$last_dir = $dir;
			chomp($dir);
			print $categories{$dir};
			print OUT '@'."[[Category:$categories{$dir}|{{PAGENAME}}]]\n";		
		}
		s/.mapp$//;
		print OUT ">$_\n";
	}
}

sub getBottomDir {
	my $dir = shift;
	$dir =~ m|/([^/]*)$|;
	$1;
}
