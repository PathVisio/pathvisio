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


$im = @imagecreate(1000, 500)
    or die("Cannot Initialize new GD image stream");
$background_color = imagecolorallocate($im, 255, 255, 255);
$text_color = imagecolorallocate($im, 233, 14, 91);
$endx = 500;
$endy = 200;
$tan = $endy/$endx;
//imagestring($im, 10,10, 5,  rad2deg(atan($tan)), $text_color);
imagestring($im, 15,15, 5,  (5/(cos(atan($tan)))), $text_color);
$point2_x = $endx + ($tan*5);
$point2_y = $endy - 5;
$point3_x = $endx - ($tan*5);
$point3_y = $endy + 5;

arrow($im, 0,0,500,200, 3,3 , 3);
header("Content-type: image/png");
imagepng($im);
imagedestroy($im);
?> 


