<?php

$webstart = file_get_contents("downloader.jnlp");

$file = $_GET['file'];
$type = $_GET['type'];

if(!$file) exit("Error: No file specified!");
if(!$type) $type = "NA";

$webstart = str_replace("FILE", $file, $webstart);
$webstart = str_replace("TYPE", $type, $webstart);

header("Content-type: application/x-java-jnlp-file");
header("Content-Disposition: attachment; filename=\"downloader.jnlp\"");
echo $webstart;
?>
