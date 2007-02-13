<?php
function arrow($svg, $x1, $y1, $x2, $y2, $alength, $awidth, $color) {

   $distance = sqrt(pow($x1 - $x2, 2) + pow($y1 - $y2, 2));

   $dx = $x2 + ($x1 - $x2) * $alength / $distance;
   $dy = $y2 + ($y1 - $y2) * $alength / $distance;

   $k = $awidth / $alength;

   $x2o = $x2 - $dx;
   $y2o = $dy - $y2;

   $x3 = $y2o * $k + $dx;
   $y3 = $x2o * $k + $dy;

   $x4 = $dx - $y2o * $k;
   $y4 = $dy - $x2o * $k;

//$blue = imagecolorallocate($im, 0, 0, 255);
   //imageline($im, $x1, $y1, $dx, $dy, $color);


   // draw a polygon
$values = array(
             $x3, $y3,  // Point 3 (x, y)
           $x4, $y4,  // Point 4 (x, y)
            $x2, $y2,  // Point 5 (x, y)
           );
fwrite($svg, "<line x1=\"$x1\" y1=\"$y1\" x2=\"$dx\" y2=\"$dy\"
style=\"stroke:rgb(99,99,99);stroke-width:2\"/>");
fwrite($svg, "<polygon points=\"$x3, $y3, $x4, $y4, $x2, $y2\"
style=\"fill:#cccccc;
stroke:#000000;stroke-width:1\"/>");
//imagefilledpolygon($im, $values, 3, 3);
//


   
}

$deel_factor = 10;
$doc_complete = new DOMDocument();
$doc_complete->load('Hs_Fatty_Acid_Beta_Oxidation_1_BiGCaT.gpml');
$entry1 = $doc_complete->getElementsByTagName("Graphics");
$boardwidth = intval($entry1->item(0)->getAttribute("BoardWidth"))/$deel_factor;
$boardheight = intval($entry1->item(0)->getAttribute("BoardHeight"))/$deel_factor*4;
$svg = fopen('test.svg', 'w+');
fwrite($svg, "<?xml version=\"1.0\" standalone=\"no\"?>\n
<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20001102//EN\"\n
	\"http://www.w3.org/TR/2000/CR-SVG-20001102/DTD/svg-20001102.dtd\" >
<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"
version=\"1.0\" width=\"$boardheight\" height=\"$boardheight\">\n\n");

//print arrows
$entries = $doc_complete->getElementsByTagName("Line");
foreach ($entries as $entry){
	$start_x = intval($entry->getElementsByTagName("Graphics")->item(0)->getElementsByTagName("Point")->item(0)->getAttribute("x"))/$deel_factor;
	$start_y = intval($entry->getElementsByTagName("Graphics")->item(0)->getElementsByTagName("Point")->item(0)->getAttribute("y"))/$deel_factor;
	$stop_x = intval($entry->getElementsByTagName("Graphics")->item(0)->getElementsByTagName("Point")->item(1)->getAttribute("x"))/$deel_factor;
	$stop_y = intval($entry->getElementsByTagName("Graphics")->item(0)->getElementsByTagName("Point")->item(1)->getAttribute("y"))/$deel_factor;
	arrow($svg, $start_x, $start_y, $stop_x, $stop_y, 3, 3, 1);
}


//GeneProducts
$entries = $doc_complete->getElementsByTagName("GeneProduct");
foreach ($entries as $entry){

	$y_cor = intval($entry->getElementsByTagName("Graphics")->item(0)->getAttribute("CenterY"))/$deel_factor;
	$x_cor = intval($entry->getElementsByTagName("Graphics")->item(0)->getAttribute("CenterX"))/$deel_factor;
	$width = intval($entry->getElementsByTagName("Graphics")->item(0)->getAttribute("Width"))/$deel_factor;
	$height = intval($entry->getElementsByTagName("Graphics")->item(0)->getAttribute("Height"))/$deel_factor;
        $x_cor = $x_cor-($width/2);
	$y_cor = $y_cor-($height/2);
	fwrite($svg, "<rect x=\"$x_cor\" y=\"$y_cor\" style=\"fill:rgb(255,255,255);stroke-width:1;
	stroke:rgb(0,0,0)\" width=\"$width\" height=\"$height\"/>\n\n");

	$y_cor = intval($entry->getElementsByTagName("Graphics")->item(0)->getAttribute("CenterY"))/$deel_factor;
	$x_cor = intval($entry->getElementsByTagName("Graphics")->item(0)->getAttribute("CenterX"))/$deel_factor;
        $font = "Century.ttf";
	$fontsize = 12;
	$text = $entry->getAttribute("GeneID");
        $angle = 0;
	$wordlengtharray = imagettfbbox($fontsize, 0, $font, $text);
	$wordlength = $wordlengtharray[4]-$wordlengtharray[6];
	$wordheigtharray = imagettfbbox($fontsize, 0, $font, $text);
	$wordheigth = $wordheightarray[7]-$wordheightarray[5];
	$x_cor = $x_cor - ($wordlength/2);
	$y_cor = $y_cor + 5;
	fwrite($svg, "<text x=\"$x_cor\" y=\"$y_cor\" style=\"font-family:$font;font-size:$fontsize\"> $text
		</text>\n");
}

//Print labels
$entries = $doc_complete->getElementsByTagName("Label"); 
foreach ($entries as $entry){
	$y_cor = intval($entry->getElementsByTagName("Graphics")->item(0)->getAttribute("CenterY"))/$deel_factor;
	$x_cor = intval($entry->getElementsByTagName("Graphics")->item(0)->getAttribute("CenterX"))/$deel_factor;
	//$wordlength = strlen($entry->getAttribute("TextLabel"));
	$fontsize = intval($entry->getElementsByTagName("Graphics")->item(0)->getAttribute("FontSize"));
	$text = $entry->getAttribute("TextLabel");
	$font = $entry->getElementsByTagName("Graphics")->item(0)->getAttribute("FontName").".ttf";
	$hexcolor = $entry->getElementsByTagName("Graphics")->item(0)->getAttribute("Color");
	$r = hexdec(substr($hexcolor, 0,2));
	$g = hexdec(substr($hexcolor, 2,2));
	$b = hexdec(substr($hexcolor, 4,2));
	$angle = 0;
	$wordlengtharray = imagettfbbox($fontsize, 0, $font, $text);
	$wordlength = $wordlengtharray[4]-$wordlengtharray[6];
	$wordheigtharray = imagettfbbox($fontsize, 0, $font, $text);
	$wordheigth = $wordheightarray[7]-$wordheightarray[5];
	$x_cor = $x_cor - ($wordlength/2);
	$y_cor = $y_cor + ($wordheigth/2);
	fwrite($svg, "<text x=\"$x_cor\" y=\"$y_cor\" style=\"font-family:$font;font-size:$fontsize\"> $text
		</text>\n");
		//imagettftext($im,$fontsize, $angle, $x_cor-($wordlength/2), $y_cor-($wordheigth/2), $text_color, $font, $text);
	//	imagestring($im, 5, $x_cor, $y_cor, $text, $text_color);
}



fwrite($svg, "</svg>");
fclose($svg);

print "<object data=\"test.svg\" width=\"$boardwidth\" height=\"$boardheight\" 
type=\"image/svg+xml\"
codebase=\"http://www.adobe.com/svg/viewer/install/\" />";



?>
