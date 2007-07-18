#!/usr/bin/perl

use warnings;
use strict;

use File::Temp qw/ tempfile /;

# Test Gpmldiff and Patch.


sub print_usage()
{
	print "patchtest.pl
Tests patch and gpmldiff on two pathways.

Usage:
  patchtest.pl old.gpml new.gpml
";	
}

my $fnOld = $ARGV[0];
my $fnNew = $ARGV[1];

if (!(-r $fnOld && -r $fnNew))
{
	print_usage();
	die;
}

my $fnDgpml = new File::Temp (SUFFIX => '.dgpml');
my $fnOldCopy = new File::Temp (SUFFIX => '.gpml');
my $fnNewCopy = new File::Temp (SUFFIX => '.gpml');

copy ($fnOld, $fnOldCopy);
copy ($fnNew, $fnNewCopy);

system ("gpmldiff.sh $fnOldCopy $fnNewCopy > $fnDgpml");

system ("patch.sh $fnOldCopy < $fnDgpml");
system ("patch.sh $fnNewCopy --reverse < $fnDgpml");

# compare fnNew with fnOldCopy
# (need to implement Pathway.equals for that)
# compare fnOld with fnNewCopy
