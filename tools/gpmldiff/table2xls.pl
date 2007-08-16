#!/usr/bin/perl

=item

table2xls can handle the table output of gpmldiff
and turn it into an excel sheet.

Usage example:

 ./gpmldiff.sh \
    -o table \
	-s better \
	testcases/Hs_Wnt_signaling.gpml 
	testcases/Hs_Wnt_signaling_modified.gpml 
 | 
	./table2xls.pl better.xls
=cut

use strict;
use warnings;

use Spreadsheet::WriteExcel::Big;
use Spreadsheet::WriteExcel::Utility;

#################
#   config
#################


my $fnOut = "output.xls";

if (@ARGV == 1)
{
	$fnOut = shift @ARGV;
}

print "Writing excel sheet to $fnOut\n";

my $workbook = Spreadsheet::WriteExcel::Big->new($fnOut);

my $header_format = $workbook->add_format(); # Add a format
$header_format->set_text_wrap();
$header_format->set_size (7);
$header_format->set_bg_color ('yellow');

my $data_format1 = $workbook->add_format();

my $data_format2 = $workbook->add_format();
$data_format2->set_bold();

my $worksheet = $workbook->add_worksheet("Similarity Matrix");

my $row = 0;
my $col = 0;
my $maxcol = 0;

while ($_ = <STDIN>)
{
	chomp;
	my @fields = split /\t/;
	
	for my $field (@fields)
	{
		my $format;
		
		if ($col == 0 || $row == 0)
		{
			$format = $header_format;
		}
		elsif ($field >= 60)
		{
			$format = $data_format2;
		}
		else
		{
			$format = $data_format1;
		}
		$worksheet->write($row, $col, $field, $format);
		$col++;
	}
	# record maximum row width
	if ($col > $maxcol) { $maxcol = $col; }
	
	$col = 0;
	$row++;
}
my $maxrow = $row;

$worksheet->freeze_panes(1, 1);
# set column width
$worksheet->set_column (1, $maxcol - 1, 12);
$worksheet->set_column (0, 1, 20);


for $row (1 .. $maxrow-1)
{
	$worksheet->write_formula ($row, $maxcol, 
		"=MAX(" .xl_rowcol_to_cell($row, 1) . ":" 
		. xl_rowcol_to_cell($row, $maxcol - 1). ")", $data_format1);
	$worksheet->write_formula ($row, $maxcol+1, 
		"=COUNTIF(" .xl_rowcol_to_cell($row, 1) . ":" 
		. xl_rowcol_to_cell($row, $maxcol - 1) . "," .
		xl_rowcol_to_cell($row, $maxcol) . ")", $data_format1);
}

for $col (1 .. $maxcol-1)
{
	$worksheet->write_formula ($maxrow, $col, 
		"=MAX(" .xl_rowcol_to_cell(1, $col) . ":" 
		. xl_rowcol_to_cell($maxrow - 1, $col). ")", $data_format1);
	$worksheet->write_formula ($maxrow + 1, $col, 
		"=COUNTIF(" .xl_rowcol_to_cell(1 , $col) . ":" 
		. xl_rowcol_to_cell($maxrow - 1, $col) . "," .
		xl_rowcol_to_cell($maxrow, $col) . ")", $data_format1);
}


$workbook->close();


if ($^O eq "MSWin32") 
{
	system ("start", $fnOut);
}
else
{
	#this works on ubuntu breezy linux, and probably other current gnome-based distros
	system ("gnome-open", $fnOut);
}
