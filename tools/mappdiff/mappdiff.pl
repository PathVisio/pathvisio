#!/usr/bin/perl

use strict;
use warnings;

use Data::Dumper;
use Win32::ODBC;
use DBI;
use File::Basename;

=pod

=head1 name

mappdiff.pl - compare two GenMAPP mapp files.

This tool should be flexible enough to handle all kinds 
of differences:
insertion of objects
deletion of objects
change of one or a few fields of an object

mappdiff.pl returns the number of differences found as exit code

=head1 usage

 mappdiff.pl file1 file2 

=cut

my $magic = 1; 
# turn on special comparisons to handle all kind of GenMAPP weirdnesses.
# this will:
#
# - compare only the lower 4 bits when comparing Systemcodes of labels


if (!(@ARGV == 2 && -r $ARGV[0] && -r $ARGV[1])) 
{ 
	die "Incorrect number of command line arguments, ".
		"or one of the files is not readable\n"; 
}

# connect to gdb
my $g_cDsn = 1;
sub get_db_handle
{
	my $fn = shift;
	
	my $odbc_dsn = "temp_dsn_for_gdb_" . $g_cDsn++;
	
	Win32::ODBC::ConfigDSN(
		Win32::ODBC::ODBC_ADD_DSN, 
		"Microsoft Access Driver (*.mdb)",
		"DSN=" . $odbc_dsn,
		"DBQ=" . $fn
	) or die "ODBC error on file $fn, $!";
	
	my $dbh = DBI->connect( "DBI:ODBC:" . $odbc_dsn, '', '', { RaiseError => 1, LongReadLen => 2000 });
	unless ($dbh) { die; }	
	return $dbh;
}

my $fnMapp1 = $ARGV[0];
my $fnMapp2 = $ARGV[1];

# read all object data from mapp 1
my @data1;
my $info1;
{
	my $dbh1 = get_db_handle ($fnMapp1);
	my $sth = $dbh1->prepare("SELECT * FROM OBJECTS");
	$sth->execute() or die "SQL error: $DBI::errstr";
	while (my $row = $sth->fetchrow_hashref)
	{
		push @data1, $row;
	}
	@data1 = sort by_object_row @data1;
	$sth = $dbh1->prepare("SELECT * FROM INFO");
	$sth->execute() or die "SQL error: $DBI::errstr";
	$info1 = $sth->fetchrow_hashref;	
}
	
#read all object data from mapp 2
my @data2;
my $info2;
{
	my $dbh2 = get_db_handle ($fnMapp2);
	my $sth = $dbh2->prepare("SELECT * FROM OBJECTS");
	$sth->execute() or die "SQL error: $DBI::errstr";
	while (my $row = $sth->fetchrow_hashref)
	{
		push @data2, $row;
	}
	@data2 = sort by_object_row @data2;
	$sth = $dbh2->prepare("SELECT * FROM INFO");
	$sth->execute() or die "SQL error: $DBI::errstr";
	$info2 = $sth->fetchrow_hashref;	
}

# calculate a match score between two object rows
# if this is below a certain value, assumed to be completely different
# if this is above a certain value, assumed that just a few fields changed
sub object_rows_hitscore
{
	my $row1 = shift;
	my $row2 = shift;
	my $score = 0;
	for (qw(ID SystemCode Type CenterX CenterY SecondX SecondY Width Height Rotation Color Label Head Remarks Image Links Notes))
	{
		if (magic_eq ($row1, $row2, $_)) { $score++; }
	}
	return $score;
}

sub magic_cmp
{
	my $row1 = shift;
	my $row2 = shift;
	my $col = shift;
	
	my $a = $$row1{$col};
	my $b = $$row2{$col};
	
	if ($magic)
	{
		no warnings;
		if ($col eq "SystemCode" && $$row1{Type} eq "Label")
		{
			#compare only lower 4 bits of systemcode for labels
			$a = ord ($a) & 0xF;
			$b = ord ($b) & 0xF;
		}
		if ($col eq "SystemCode" && $$row1{Type} eq "Gene")
		{
			# trim trailing whitespace
			$a =~ s/\s*$//;
			$b =~ s/\s*$//;
		}
		elsif ($col eq "Color" && $$row1{Type} eq "Gene")
		{
			#ignore color field for genes
			$a = 0;
			$b = 0;
		}
		elsif ($col =~ /^(Remarks|Width|Height)$/ && $$row1{Type} eq "InfoBox")
		{
			#ignore most fields for InfoBox
			$a = 0;
			$b = 0;
		}
		elsif ($col eq "Height" && $$row1{Type} =~ /^(Vesicle|Poly)$/)
		{
			#ignore height for some shapes like Vesicle and Poly, only width counts.
			$a = 0;
			$b = 0;
		}
		elsif ($col eq "Color" && $$row1{Type} =~ /^(Vesicle)$/)
		{
			#ignore color for some shapes.
			$a = 0;
			$b = 0;
		}
		elsif ($col eq "Head" && $$row1{Type} eq "Label")
		{
			# trim trailing whitespace
			$a =~ s/\s*$//;
			$b =~ s/\s*$//;
		}
	}
	
	no warnings;
	return ($a cmp $b);
}

sub magic_eq
{
	return (magic_cmp (@_) == 0);
}

# Compares two rows column by column
# prints all columns that differ
sub object_row_diff
{
	my $row1 = shift;
	my $row2 = shift;
	my $output;
	for (qw(ID SystemCode Type CenterX CenterY SecondX SecondY Width Height Rotation Color Label Head Remarks Image Links Notes))
	{
		no warnings;
		unless (magic_eq ($row1, $row2, $_)) 
		{
			if ($$row1{Type} eq "Label" && $_ eq "SystemCode")
			{
				$output .= "Column $_ : '" . ord($$row1{$_}) . "' <-> '" . ord($$row2{$_}) . "'\n";
			}
			else
			{
				$output .= "Column $_ : '" . $$row1{$_} . "' <-> '" . $$row2{$_} . "'\n";
			}
		}
	}
	if ($output)
	{
		print "In row Type<", $$row1{Type}, "> ID<", $$row1{ID}, "> Label<", $$row1{Label}, 
			"> CenterX<", $$row1{CenterX}, ">\n", $output;
	}
}

# Compares two rows column by column
# prints all columns that differ
sub info_row_diff
{
	my $row1 = shift;
	my $row2 = shift;
	my $output;
	for (qw(Title MAPP GeneDB GeneDBVersion Version Author Maint Email Copyright Modify Remarks BoardWidth BoardHeight WindowWidth WindowHeight Notes))
	{
		no warnings;
		unless (magic_eq ($row1, $row2, $_)) 
		{
			$output .= "Column $_ : '" . $$row1{$_} . "' <-> '" . $$row2{$_} . "'\n";
		}
	}
	if ($output)
	{
		print "In Info row: \n$output";
		return 1;
	}
	return 0;
}

# sorting routine
# Sorts on all columns, the ones most unlikely to change first
sub by_object_row ($$)
{
	my $a = shift;
	my $b = shift;
	for my $col (qw(Type ID CenterX CenterY SystemCode SecondX SecondY Width Height Rotation Color Label Head Remarks Image Links Notes))
	{
		no warnings;
		my $result = magic_cmp ($a, $b, $col);
		if ($result != 0) { return $result; }
	}	
}

my $diffCount = 0;

$diffCount += info_row_diff ($info1, $info2);

while (@data1 > 0 || @data2 > 0)
{
	my $row1 = shift @data1;
	my $row2 = shift @data2;

	my $score = object_rows_hitscore ($row1, $row2);
	if ($score != 17)
	{
		$diffCount ++;
	}
	if ($score >= 14)
	{
		object_row_diff ($row1, $row2);
	}
	else
	{
		my $cmp = by_object_row ($row1, $row2);
		if ($cmp < 0 || !defined($row2))
		{
			print "Unique in 1: ";
			print "row Type='", $$row1{Type}, "' ID='", $$row1{ID}, "' CenterX='", 
				$$row1{CenterX}, "' CenterY='", $$row1{CenterY}, "'\n";
			unshift @data2, $row2;
		}
		else
		{
			print "Unique in 2: ";
			print "row Type='", $$row2{Type}, "' ID='", $$row2{ID}, "' CenterX='", 
				$$row2{CenterX}, "' CenterY='", $$row2{CenterY}, "'\n";
			unshift @data1, $row1;
		}
	}
}

print "Found $diffCount differences\n";
exit ($diffCount);
