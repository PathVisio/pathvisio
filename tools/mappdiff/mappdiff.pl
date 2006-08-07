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

=head1 usage

 mappdiff.pl file1 file2

=cut

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
	) or die $!;
	
	my $dbh = DBI->connect( "DBI:ODBC:" . $odbc_dsn );
	unless ($dbh) { die; }	
	return $dbh;
}

my $fnMapp1 = $ARGV[0];
my $fnMapp2 = $ARGV[1];

my $dbh1 = get_db_handle ($fnMapp1);
my $dbh2 = get_db_handle ($fnMapp2);


my @data1;
{
	my $sth = $dbh1->prepare("SELECT * FROM OBJECTS");
	$sth->execute() or die "SQL error: $DBI::errstr";
	while (my $row = $sth->fetchrow_hashref)
	{
		push @data1, $row;
	}
	@data1 = sort by_object_row @data1;
}
	
my @data2;
{
	my $sth = $dbh2->prepare("SELECT * FROM OBJECTS");
	$sth->execute() or die "SQL error: $DBI::errstr";
	while (my $row = $sth->fetchrow_hashref)
	{
		push @data2, $row;
	}
	@data2 = sort by_object_row @data2;
}

sub object_rows_hitscore
{
	my $row1 = shift;
	my $row2 = shift;
	my $score = 0;
	for (qw(ID SystemCode Type CenterX CenterY SecondX SecondY Width Height Rotation Color Label Head Remarks Image Links Notes))
	{
		if ($$row1{$_} eq $$row2{$_}) { $score++; }
	}
	return $score;
}

sub object_row_diff
{
	my $row1 = shift;
	my $row2 = shift;
	for (qw(ID SystemCode Type CenterX CenterY SecondX SecondY Width Height Rotation Color Label Head Remarks Image Links Notes))
	{
		no warnings;
		unless ($$row1{$_} eq $$row2{$_}) 
		{
			print "Col $_ : '", $$row1{$_}, "' <-> '", $$row2{$_}, "'\n";
		}
	}
}

sub by_object_row
{
	for my $col (qw(Type ID CenterX CenterY SystemCode SecondX SecondY Width Height Rotation Color Label Head Remarks Image Links Notes))
	{
		no warnings;
		print exists $$a{$col} ? "" : $col;
		my $result = $$a{$col} cmp $$b{$col};
		if ($result != 0) { return $result; }
	}	
}

print scalar @data1, "\t", scalar @data2, "\n";

for (1..4)
{	
	object_row_diff (shift @data1, shift @data2);
}

#~ while (1)
#~ {

	
	
	#~ my $max_score = 0;
	#~ my $max_row = undef;
	#~ my $max_index = undef;
	#~ my $type = $$row{Type};

	#~ for (my $i = 0; $i < scalar @{$data2{$type}}; ++$i)
	#~ {
		#~ my $row = ${$data2{$type}[$i];
		#~ my $score = object_rows_hitscore ($row, $row2)		
		#~ if ($score > $max_score)
		#~ {
			#~ $max_score = $score;
			#~ $max_row = $row2;
			#~ $max_index = $i;
		#~ }		
	#~ }
		
	#~ if ($max_score > 14)
	#~ {
		#~ #not so perfect.
		#~ #which cols are differing?
		#~ object_row_diff ($max_row, $row);
		#~ splice (@{$data2{$type}}, $max_index, 1);
	#~ }
	#~ else
	#~ {
		#~ #nothing matches
		#~ print "Only in 1:\n", Dumper ($row);
	#~ }
#~ }


