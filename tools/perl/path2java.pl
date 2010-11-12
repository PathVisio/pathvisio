#!/usr/bin/perl

use strict;
use warnings;
use Cwd;

### This script parses java path coordinates from JavaFX files created in Inkscape.

### For the coordinates to work optimally in PathVisio, these are the requirements on the Inkscape graphic:
### 1. Draw objects as close to 0,0 as possible.
### 2. The overall shape of the object in Inkscape should be that of a square or rectangle, to fit into pre-defined shape in PathVisio.
### 3. Size the object with a width and height < 500 pixels.
### 4. Save the file as .svg, then save as .fx (JavaFX).
### 5. Confirm that the coordinates in the JavaFX file are all positive. Negative numbers are not handled by this script.

### INPUT: Set of javafx files in same directory as script
### OUTPUT: One text file per input file, with the suffix "-parsed_path". 
### Script also writes to terminal for easy access to results.

#### FIND Files
my @files = <*.fx>;
print "list of files is @files\n";

foreach my $file (@files)
{
	print "processing $file\n";
	
	unless ( open(FILE, $file) )
      	{
        print "could not open file $file\n";
        exit;
       	}
	
	## Define output file
	my $filename = (split('\.', $file))[0];
	my $outfile = $filename."-parsed_path.txt";	  
	unless ( open(OUTFILE, ">$outfile") )
       	{
      	print "could not open file $outfile\n";
      	exit;
 		}

	## Concatenate file contents into one string
	
	my $string = ();
    while (my $line = <FILE>)
      {
      $string .= $line;
      }
      
    ## Find definition of paths in file. Handles multiple paths corresponding to multiple objects.
    $string =~ /.+CustomNode(.+)override/s;
	my $paths = $1;
	 	  
	my @pathchunks = split('/', $paths);
	my @pathdata = ();
	  
	## Get the relevant pieces of path data
	foreach my $p (@pathchunks)
		{
		if ($p =~ /Path\s\{/){
			push (@pathdata, $p);
		}
	  	}
	  
	  
	foreach my $pd (@pathdata)
	  	{
	  	my $coords = (split('elements',$pd))[1];
	  
	  	## Remove spaces
	  	$coords =~ s/\s+//gs;
	  	
	  	## Get individual lines of coordinate sets
	  	my @coords = split(',',$coords);
	  	
	  	foreach my $c (@coords)
	  		{
	  		if ($c =~ /MoveTo/)
	  		{
	  			$c =~ /x:(\d+\.\d+)y/;
	  			my $startx = sprintf("%.2f", $1);  ## Round to two decimals
	  			$c =~ /y:(\d+\.\d+)}/;
	  			my $starty = sprintf("%.2f", $1);
	  			print OUTFILE "path.moveTo ($startx"."f, "."$starty"."f);\n";
	  			print "path.moveTo ($startx"."f, "."$starty"."f);\n";
	  		}
	  		if ($c =~ /CubicCurveTo/)
	  		{
	  			$c =~ /controlX1:(\d+\.\d+)control/;
	  			my $x1 = sprintf("%.2f", $1);
	  			$c =~ /controlY1:(\d+\.\d+)control/;
	  			my $y1 = sprintf("%.2f", $1);
	  			$c =~ /controlX2:(\d+\.\d+)control/;
	  			my $x2 = sprintf("%.2f", $1);
	  			$c =~ /controlY2:(\d+\.\d+)x/;
	  			my $y2 = sprintf("%.2f", $1);
	  			$c =~ /x:(\d+\.\d+)y/;
	  			my $x3 = sprintf("%.2f", $1);
	  			$c =~ /y:(\d+\.\d+)}/;
	  			my $y3 = sprintf("%.2f", $1);
	  		
				print OUTFILE "path.curveTo ($x1"."f, "."$y1"."f, "."$x2"."f , "."$y2"."f, "."$x3"."f , "."$y3"."f);\n";
	  			print "path.curveTo ($x1"."f, "."$y1"."f, "."$x2"."f , "."$y2"."f, "."$x3"."f , "."$y3"."f);\n";
	  		
	  		}
	  		if ($c =~ /LineTo/)
	  		{
	  			$c =~ /x:(\d+\.\d+)y/;
	  			my $x = sprintf("%.2f", $1);
	  			$c =~ /y:(\d+\.\d+)}/;
	  			my $y = sprintf("%.2f", $1);
	  			print OUTFILE "path.lineTo ($x"."f, "."$y"."f);\n";
	  			print "path.lineTo ($x"."f, "."$y"."f);\n";
	  		}
	  	} 
	  	print OUTFILE "path.closePath();\n";
		print "path.closePath();\n";
	}
}

print "\n\n\nDone!";
close OUTFILE;