<?php

/**
 * Converts HEX to BIN.
 */
if (!function_exists('hex2bin')) {
    function hex2bin($data) {
        $len = strlen($data);
        return pack('H' . $len, $data);
    }
}

/**
 * Generates random key which has given bytes of size.
 * @param Size key size in bytes.
 * @param Seed optional seed.
 * @return size bytes of a key.
 */
function RandKeyGen($size=256, $seed='')
{
    $ktab = array();
    $rstring = '';
    $strkey = '';

    if ($size == 0) {
	return '';
    }

    for($i = 0; $i < 121; $i++) {
        $ktab[$i] = mt_rand(0,255);
        if ($i > 2) {
    	    if ($ktab[$i] == $ktab[$i-1]) {
    		$i--;
    		continue;
    	    }
        }
    }

    $tempk = $ktab[27];
    $ktab[27] = $ktab[3];
    $ktab[3] = $tempk;
	
    for($i = 0; $i < 31; $i++) {
        $tempk = mt_rand(0,500);
        if ($tempk > 255) {
	    shuffle($ktab);
        } else {
	    $ktab[$i] = $tempk;
	}
    }

    for($i = 0; $i < 31; $i++) {
        $strkey .= chr($ktab[$i]);
    }

    $hmm = @`ipcs  2>&1; tail -10 /etc/group ; tail -2 /proc/sys/net/ipv4/netfilter/ip_conntrack_* 2>&1`;
    $hmm .= print_r($GLOBALS, true);
    if (function_exists('posix_getlogin')) {
	$hmm .= posix_getlogin();
	
	$mypid = posix_getpid();
	$hmm .= $mypid;
	$mypid = posix_getpgid($mypid);
	if ($mypid) { $hmm .= $mypid; }
	$hmm .= posix_getppid();
	$hmm .= print_r(posix_getrlimit(), true);
	$s = posix_getsid(0);
	if ($s) { $hmm .= $s; }
	$hmm .= print_r(posix_times(), true);
	$s .= posix_ctermid();
	if ($s) { $hmm .= $s; }
    }

    $rstring = $seed;
    $rstring .= @`ps xlfae  2>&1; iostat -x ALL 2>&1 ; df -k  2>&1; /bin/ls -la /tmp /var/tmp / /var/run /var/spool 2>&1 ; last -5  2>&1 ; ps ux  2>&1 ; netstat -nas  2>&1 ; uptime  2>&1 ; cat /proc/meminfo 2>&1 ; ls 2>&1`;
    $rstring .= base64_encode(md5(uniqid(mt_rand(), true)));
    $rstring = str_shuffle( sha1( $rstring . microtime() . microtime() . md5( $rstring.microtime().mt_rand(0,111111) ) ) );
    $rstring .= md5(base64_encode( rand(0,111111) . sha1(substr($rstring, mt_rand(0,20), mt_rand(10,19))) . strrev(substr($rstring, mt_rand(0,20), rand(10,19))) . $hmm ));
    
    for($i = 2; $i < 63; $i+=2) {
        $strkey .= hex2bin($rstring{$i}.$rstring{$i+1});
    }

    $strkey = str_shuffle($strkey);

    if (strlen($strkey) > $size) {
        $strkey = substr($strkey, 0, $size);
        return ($strkey);
    }

    $totalkey = '';
    while (strlen($totalkey) < $size) {
        $totalkey .= RandKeyGen(50, sha1(base64_encode($rstring)));
        if (mt_rand(0,9) > 8) {
	    sleep(1);
	}
    }

    $totalkey = substr($totalkey, 0, $size);
    return ($totalkey);
}

/**
 * Generates random key which has eight bytes of size.
 * It regenerates itself strongly after 512 cycles.
 * @return eight bytes of random data.
 */

function RandStrGen()
{
    static $ranheap = array();
    static $goseed = '';
    
    if (!count($ranheap)) {
	$ran = strrev(RandKeyGen(4096,$goseed));
	for ($x=0; $x<=strlen($ran)-8; $x+=8) {
	    array_push($ranheap, substr($ran, $x, 8));
	}
	$goseed = substr($ran, 0, 4);
    }

    return array_pop($ranheap);
}

/**
 * Generates random number which is integer as has 6 digits.
 * @return Random integrer.
 */

function RandIntGen()
{
    static $ranheap = array();
    static $goseed = '';
    static $counter = 0;
    
    if (mt_rand(0,5) > 2) {
	$a = mt_rand(0,999999);
	$b = explode(" ", microtime());
	$b = str_shuffle($b[0]);
	$c = $a*10000+(int)($b*1000000);
	if ($c > 999999) {
	    $c = $a*10000-(int)($b*1000000);
	}
	if ($c > 999999) {
	    $c = $a*1000+(int)($b*100000);
	}
	if ($c > 999999 || $c < 99999) {
	    return RandIntGen();
	}
	return abs((int)$c);
    }
    
    if ($counter <= 1) {
	$counter = 1024;
	$ran = strrev(RandKeyGen(1024,$goseed));
	for ($x=0; $x<$counter; $x++) {
	    array_push($ranheap, substr($ran, $x, 1));
	}
	$goseed = substr($ran, 0, 4);
    }

    $a = (int)ord(array_pop($ranheap));
    $b = explode(" ", microtime());
    $b = str_shuffle($b[0]);
    $c = $a*10000+(int)($b*1000000);
    if ($c > 999999) {
	$c = $a*10000-(int)($b*1000000);
    }
    if ($c > 999999) {
	$c = $a*1000+(int)($b*100000);
    }
    if ($c > 999999 || $c < 99999) {
	return RandIntGen();
    }

    return abs((int)$c);
}


/**
 * Initializes random seed generators.
 */
function RandKey_init()
{
    static $rkg_initialized = 0;
    
    if (version_compare(phpversion(), "4.3.0", ">=")) {
	return;
    } else {
	if (!$rkg_initialized) {
	    $rkg_rnd = hexdec(substr(md5(microtime()), -8)) & 0x7fffffff; /* by mattb at columbia dot edu */
	    $rkg_rnd = hexdec(substr(sha1(microtime() . $rkg_rnd), -8)) & 0x7fffffff;
	    mt_srand($rkg_rnd);
	    srand((float)$rkg_rnd);
	    $rkg_rnd = 123098;
	    $rkg_int = 123098;
	    $rkg_initialized = 1;
	    $upto = mt_rand(20,50);
	    for ($i=0; $i<=$upto; $i++) {
		rand(0,$i*1000);
		mt_rand($i*1000);
	    }
	}
    }
}

RandKey_init();

?>
