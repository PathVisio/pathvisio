<?php

function arrow($im, $x1, $y1, $x2, $y2, $alength, $awidth, $color) {

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

$blue = imagecolorallocate($im, 0, 0, 255);
   imageline($im, $x1, $y1, $dx, $dy, $color);


   // draw a polygon
$values = array(
             $x3, $y3,  // Point 3 (x, y)
           $x4, $y4,  // Point 4 (x, y)
            $x2, $y2,  // Point 5 (x, y)
           );
imagefilledpolygon($im, $values, 3, 3);
   
}

$doc_complete = new DOMDocument();
$doc_complete->load($_FILES['userfile']['tmp_name']);
$entry1 = $doc_complete->getElementsByTagName("Graphics");
$boardwidth = intval($entry1->item(0)->getAttribute("BoardWidth"))/15;
$boardheight = intval($entry1->item(0)->getAttribute("BoardHeight"))/15;


$im = @imagecreatetruecolor($boardwidth,$boardheight)
	or die("Cannot Initialize new GD image stream");
$background_color = imagecolorallocate($im, 255, 255, 255);
imagefilledrectangle($im, 0, 0, $boardwidth,$boardheight, $background_color);
$text_color = imagecolorallocate($im, 233, 14, 91);

//Print labels
$entries = $doc_complete->getElementsByTagName("Label"); 
foreach ($entries as $entry){
	$y_cor = intval($entry->getElementsByTagName("Graphics")->item(0)->getAttribute("CenterY"))/15;
	$x_cor = intval($entry->getElementsByTagName("Graphics")->item(0)->getAttribute("CenterX"))/15;
	//$wordlength = strlen($entry->getAttribute("TextLabel"));
	$fontsize = intval($entry->getElementsByTagName("Graphics")->item(0)->getAttribute("FontSize"));
	$text = $entry->getAttribute("TextLabel");
	$font = $entry->getElementsByTagName("Graphics")->item(0)->getAttribute("FontName").".ttf";
	$hexcolor = $entry->getElementsByTagName("Graphics")->item(0)->getAttribute("Color");
	$r = hexdec(substr($hexcolor, 0,2));
	$g = hexdec(substr($hexcolor, 2,2));
	$b = hexdec(substr($hexcolor, 4,2));
	$text_color = imagecolorallocate($im, $r, $g, $b);
	$angle = 0;
	$wordlengtharray = imagettfbbox($fontsize, 0, $font, $text);
	$wordlength = $wordlengtharray[4]-$wordlengtharray[6];
	$wordheigtharray = imagettfbbox($fontsize, 0, $font, $text);
	$wordheigth = $wordheightarray[7]-$wordheightarray[5];
	imagettftext($im,$fontsize, $angle, $x_cor-($wordlength/2), $y_cor-($wordheigth/2), $text_color, $font, $text);
//	imagestring($im, 5, $x_cor, $y_cor, $text, $text_color);
}

//print geneproducts
$entries = $doc_complete->getElementsByTagName("GeneProduct");
foreach ($entries as $entry){
	$y_cor = intval($entry->getElementsByTagName("Graphics")->item(0)->getAttribute("CenterY"))/15;
	$x_cor = intval($entry->getElementsByTagName("Graphics")->item(0)->getAttribute("CenterX"))/15;
	$width = intval($entry->getElementsByTagName("Graphics")->item(0)->getAttribute("Width"))/15;
	$height = intval($entry->getElementsByTagName("Graphics")->item(0)->getAttribute("Height"))/15;
	imagerectangle ($im, $x_cor-($width/2), $y_cor-($height/2), $x_cor+($width/2), $y_cor+($height/2), 1);
	$fontsize = 12;
	$text = $entry->getAttribute("GeneID");
        $angle = 0;
	$wordlengtharray = imagettfbbox($fontsize, 0, $font, $text);
	$wordlength = $wordlengtharray[4]-$wordlengtharray[6];
	$wordheigtharray = imagettfbbox($fontsize, 0, $font, $text);
	$wordheigth = $wordheightarray[7]-$wordheightarray[5];
	imagettftext($im,$fontsize, $angle, $x_cor-($wordlength/2), $y_cor-($wordheigth/2), $text_color, $font, $text);
//	imagestring($im, 5, $x_cor-($width/2), $y_cor-($height/2), $text , $text_color);
}

//print arrows
$entries = $doc_complete->getElementsByTagName("Line");
foreach ($entries as $entry){

	$start_x = intval($entry->getElementsByTagName("Graphics")->item(0)->getElementsByTagName("Point")->item(0)->getAttribute("x"))/15;
	$start_y = intval($entry->getElementsByTagName("Graphics")->item(0)->getElementsByTagName("Point")->item(0)->getAttribute("y"))/15;
	$stop_x = intval($entry->getElementsByTagName("Graphics")->item(0)->getElementsByTagName("Point")->item(1)->getAttribute("x"))/15;
	$stop_y = intval($entry->getElementsByTagName("Graphics")->item(0)->getElementsByTagName("Point")->item(1)->getAttribute("y"))/15;
	arrow($im, $start_x, $start_y, $stop_x, $stop_y, 3, 3, 1);

}

//Infobox

imagestring($im, 5, 500, 1,  "Name: ".$doc_complete->getElementsByTagName("Pathway")->item(0)->getAttribute("Name"), $text_color);
imagestring($im, 5, 500, 15,  "Author: ".$doc_complete->getElementsByTagName("Pathway")->item(0)->getAttribute("Author"), $text_color);
imagestring($im, 5, 500, 30,  "Maintained by: ".$doc_complete->getElementsByTagName("Pathway")->item(0)->getAttribute("Maintained-By"), $text_color);
imagestring($im, 5, 500, 45,  "Email: ".$doc_complete->getElementsByTagName("Pathway")->item(0)->getAttribute("Email"), $text_color);
imagestring($im, 5, 500, 60,  "Availability: ".$doc_complete->getElementsByTagName("Pathway")->item(0)->getAttribute("Availability"), $text_color);
imagestring($im, 5, 500, 75,  "Last Modified: ".$doc_complete->getElementsByTagName("Pathway")->item(0)->getAttribute("Last-Modified"), $text_color);





header("Content-type: image/png");
imagepng($im);
imagedestroy($im);
?> 

