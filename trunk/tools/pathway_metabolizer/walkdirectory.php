<?php
set_time_limit(0);
function read_dir($dir) {
   $array = array();
      $d = dir($dir);
      while (false !== ($entry = $d->read())) {
      if($entry!='.' && $entry!='..') {
                 $entry = $entry;
		 if(is_dir($entry)) {
		 //$array[] = $entry;
		 $array = array_merge($array, read_dir($entry));
		 } else {
		 $array[] = $entry;
		 }
	 }}
	 $d->close();
	 return $array;
	 }
	
$all_files =array();
$all_files=read_dir("pathways");
$i = 0;
foreach ($all_files as $file){
	//print $file."<BR>";
	//print "http://127.0.0.1/gpml_annotation/entity_annotation_2.php?pathway=$file";
	print file_get_contents("http://127.0.0.1/gpml_annotation/entity_annotation_2.php?pathway=$file");
	$i++;
}
print "<PRE>";
print_r($all_files);
print "</pre>";
?>