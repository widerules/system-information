package simple.home.jtbuaa;

import java.util.HashMap;

public class FileType {

	public static HashMap<String, String> MIMEMAP 
		= new HashMap<String, String>();

	public final static void initMimeMap(){
		MIMEMAP.put("MP3", "audio/mpeg");
		MIMEMAP.put("M4A",  "audio/mp4");
		MIMEMAP.put("WAV",  "audio/x-wav");
		MIMEMAP.put("AMR",  "audio/amr");
		MIMEMAP.put("AWB",  "audio/amr-wb");
		MIMEMAP.put("WMA",  "audio/x-ms-wma");    
		MIMEMAP.put("3GA",  "audio/3gpp");    
		MIMEMAP.put("OGG",  "application/ogg");    

		MIMEMAP.put("MID",  "audio/midi");
		MIMEMAP.put("MIDI",  "audio/midi");
		MIMEMAP.put("XMF",  "audio/midi");
		MIMEMAP.put("RTTTL",  "audio/midi");
		MIMEMAP.put("SMF",  "audio/sp-midi");
		MIMEMAP.put("IMY",  "audio/imelody");
		
		MIMEMAP.put("MP4",  "video/mp4");
		MIMEMAP.put("M4V",  "video/mp4");
		MIMEMAP.put("3GP",  "video/3gpp");
		MIMEMAP.put("3GPP",  "video/3gpp");
		MIMEMAP.put("3G2",  "video/3gpp2");
		MIMEMAP.put("3GPP2",  "video/3gpp2");
		MIMEMAP.put("WMV",  "video/x-ms-wmv");

		MIMEMAP.put("JPG",  "image/jpeg");
		MIMEMAP.put("JPEG",  "image/jpeg");
		MIMEMAP.put("GIF",  "image/gif");
		MIMEMAP.put("PNG",  "image/png");
		MIMEMAP.put("BMP",  "image/x-ms-bmp");
		MIMEMAP.put("WBMP",  "image/vnd.wap.wbmp");

		MIMEMAP.put("M3U",  "audio/x-mpegurl");
		MIMEMAP.put("PLS",  "audio/x-scpls");
		MIMEMAP.put("WPL",  "application/vnd.ms-wpl");

		MIMEMAP.put("JAR",  "application/java-archive");
		MIMEMAP.put("JAD",  "text/vnd.sun.j2me.app-descriptor");

		MIMEMAP.put("APK",  "application/vnd.android.package-archive");
		MIMEMAP.put("WDGT", "vnd.android.cursor.item/vnd.borqs.widget");
		
		MIMEMAP.put("ZIP", "application/zip");
		MIMEMAP.put("DWG", "application/acad");
		MIMEMAP.put("ASD", "application/astound");
		MIMEMAP.put("ASN", "application/astound");
		MIMEMAP.put("TSP", "application/dsptype");
		MIMEMAP.put("DXF", "application/dxf");
		MIMEMAP.put("SPL", "application/futuresplash");
		MIMEMAP.put("GZ", "application/gzip");
		MIMEMAP.put("PTLK", "application/listenup");
		MIMEMAP.put("HQX", "application/mac-binhex40");
		MIMEMAP.put("MDB", "application/mbedlet");
		MIMEMAP.put("MIF", "application/mif");
		MIMEMAP.put("XLS", "application/msexcel");
		MIMEMAP.put("XLA", "application/msexcel");
		MIMEMAP.put("HLP", "application/mshelp");
		MIMEMAP.put("CHM", "application/mshelp");
		MIMEMAP.put("PPT", "application/mspowerpoint");
		MIMEMAP.put("PPS", "application/mspowerpoint");
		MIMEMAP.put("PPZ", "application/mspowerpoint");
		MIMEMAP.put("POT", "application/mspowerpoint");
		MIMEMAP.put("DOT", "application/msword");
		MIMEMAP.put("DOC", "application/msword");
		MIMEMAP.put("CLASS", "application/octet-stream");
		MIMEMAP.put("ODA", "application/oda");
		MIMEMAP.put("PDF", "application/pdf");
		MIMEMAP.put("AI", "application/postscript");
		MIMEMAP.put("EPS", "application/postscript");
		MIMEMAP.put("PS", "application/postscript");
		MIMEMAP.put("RTC", "application/rtc");
		MIMEMAP.put("RTF", "application/rtf");
		MIMEMAP.put("SMP", "application/studiom");
		
		MIMEMAP.put("TBK", "application/toolbook");
		MIMEMAP.put("VMD", "application/vocaltec-media-desc");
		MIMEMAP.put("VMF", "application/vocaltec-media-file");
		MIMEMAP.put("BCPIO", "application/x-bcpio");
		MIMEMAP.put("Z", "application/x-compress");
		MIMEMAP.put("CPIO", "application/x-cpio");
		MIMEMAP.put("CSH", "application/x-csh");
		MIMEMAP.put("DCR", "application/x-director");
		MIMEMAP.put("DIR", "application/x-director");
		MIMEMAP.put("DXR", "application/x-director");
		MIMEMAP.put("DVI", "application/x-dvi");
		MIMEMAP.put("EVY", "application/x-dvi");
		MIMEMAP.put("GTAR", "application/x-gtar");
		
		MIMEMAP.put("HDF", "application/x-hdf");
		MIMEMAP.put("PHT", "application/x-httpd-php");
		MIMEMAP.put("PHTML", "application/x-httpd-php");
		MIMEMAP.put("JS", "application/x-javascript");
		MIMEMAP.put("LATEX", "application/x-latex");
		MIMEMAP.put("BIN", "application/x-macbinary");
		MIMEMAP.put("MIF", "application/x-mif");
		MIMEMAP.put("NC", "application/x-netcdf");
		MIMEMAP.put("CDF", "application/x-netcdf");
		MIMEMAP.put("NSC", "application/x-nschat");
		MIMEMAP.put("SH", "application/x-sh");
		MIMEMAP.put("SHAR", "application/x-shar");
		MIMEMAP.put("SWF", "application/x-shockwave-flash");
		
		MIMEMAP.put("CAB", "application/x-shockwave-flash");
		MIMEMAP.put("SPR", "application/x-sprite");
		MIMEMAP.put("SPRITE", "application/x-sprite");
		MIMEMAP.put("SIT", "application/x-stuffit");
		MIMEMAP.put("SCA", "application/x-supercard");
		MIMEMAP.put("SV4CPIO", "application/x-sv4cpio");
		MIMEMAP.put("SV4CRC", "application/x-sv4crc");
		MIMEMAP.put("TAR", "application/x-tar");
		MIMEMAP.put("TCL", "application/x-tcl");
		MIMEMAP.put("TEX", "application/x-tex");
		MIMEMAP.put("TEXINFO", "application/x-texinfo");
		MIMEMAP.put("TEXI", "application/x-texinfo");
		MIMEMAP.put("T", "application/x-troff");
		
		MIMEMAP.put("TR", "application/x-troff");
		MIMEMAP.put("TOFF", "application/x-troff");
		MIMEMAP.put("MON", "application/x-troff-man");
		MIMEMAP.put("TROFF", "application/x-troff-man");
		MIMEMAP.put("ME", "application/x-troff-me");
		MIMEMAP.put("USTAR", "application/x-ustar");
		MIMEMAP.put("SRC", "application/x-wais-source");
		MIMEMAP.put("ZIP", "application/zip");
		MIMEMAP.put("AU", "audio/basic");
		MIMEMAP.put("SND", "audio/basic");
		MIMEMAP.put("ES", "audio/echospeech");
		MIMEMAP.put("TSI", "audio/tsplayer");
		MIMEMAP.put("VOX", "audio/voxware");
		
		MIMEMAP.put("AIF", "audio/x-aiff");
		MIMEMAP.put("AIFF", "audio/x-aiff");
		MIMEMAP.put("AIFC", "audio/x-aiff");
		MIMEMAP.put("DUS", "audio/x-dspeeh");
		MIMEMAP.put("CHT", "audio/x-dspeeh");
		MIMEMAP.put("MID", "audio/x-midi");
		MIMEMAP.put("MIDI", "audio/x-midi");
		MIMEMAP.put("MP2", "audio/x-mpeg");
		MIMEMAP.put("RAM", "audio/x-pn-realaudio");
		MIMEMAP.put("RA", "audio/x-pn-realaudio");
		MIMEMAP.put("RPM", "audio/x-pn-realaudio-plugin");
		MIMEMAP.put("STREAM", "audio/x-qt-stream");
		MIMEMAP.put("WAV", "audio/x-wav");
		
		MIMEMAP.put("DWF", "drawing/x-dwf");
		MIMEMAP.put("COD", "image/cis-cod");
		MIMEMAP.put("RAS", "image/cmu-raster");
		MIMEMAP.put("FIF", "image/fif");
		MIMEMAP.put("GIF", "image/gif");
		MIMEMAP.put("LEF", "image/ief");
		MIMEMAP.put("JPEG", "image/jpeg");
		MIMEMAP.put("JPE", "image/jpeg");
		MIMEMAP.put("JPG", "image/jpeg");
		MIMEMAP.put("TIFF", "image/tiff");
		MIMEMAP.put("TIF", "image/tiff");
		MIMEMAP.put("MCF", "image/vasa");
		MIMEMAP.put("WBMP", "image/vnd.wap.wbmp");
		MIMEMAP.put("FH4", "image/x-freehand");
		MIMEMAP.put("FH5", "image/x-freehand");
		MIMEMAP.put("FHC", "image/x-freehand");
		MIMEMAP.put("PNM", "image/x-portable-anymap");
		MIMEMAP.put("PBM", "image/x-portable-bitmap");
		MIMEMAP.put("PGM", "image/x-portable-graymap");
		MIMEMAP.put("PPM", "image/x-portable-pixmap");
		MIMEMAP.put("RGB", "image/x-rgb");
		MIMEMAP.put("XWD", "image/x-windowdump");
		MIMEMAP.put("XBM", "image/x-xbitmap");
		MIMEMAP.put("XPM", "image/x-xpixmap");
		MIMEMAP.put("WRL", "model/vrml");
		MIMEMAP.put("CSV", "text/comma-separated-values");
	}

}
