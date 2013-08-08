package base.lib;

import java.net.URLDecoder;

import common.lib.TitleUrl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.webkit.WebSettings.TextSize;

public class WebUtil extends util {
	
	static public String getSite(String url) {//identical
		String site = "";
		String[] tmp = url.split("/");
		// if url is http://m.baidu.com, then url.split("/")[2] is m.baidu.com
		if (tmp.length > 2)	site = tmp[2];
		else site = tmp[0];
		
		return site;
	}
	
	static public TextSize readTextSize(SharedPreferences sp) {//identical
		TextSize textSize = TextSize.NORMAL;
		int iTextSize = sp.getInt("textsize", 2);
		switch (iTextSize) {
		case 1:
			textSize = TextSize.LARGER;
			break;
		case 2:
			textSize = TextSize.NORMAL;
			break;
		case 3:
			textSize = TextSize.SMALLER;
			break;
		case 4:
			textSize = TextSize.SMALLEST;
			break;
		case 5:
			textSize = TextSize.LARGEST;
			break;
		}
		return textSize;
	}

	static public String selectUA(int ua) {//identical
		switch (ua) {
		case 2:// ipad
			return "Mozilla/5.0 (iPad; U; CPU  OS 4_1 like Mac OS X; en-us)AppleWebKit/532.9(KHTML, like Gecko) Version/4.0.5 Mobile/8B117 Safari/6531.22.7";
		case 3:// iPhone
			return "Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420+ (KHTML, like Gecko) Version/3.0 Mobile/1A543a Safari/419.3";
		case 4:// black berry
			return "Mozilla/5.0 (BlackBerry; U; BlackBerry 9800; en-US) AppleWebKit/534.1+ (KHTML, like Gecko)";
		case 5:// chrome
			return "Mozilla/5.0 (X11; Linux i686) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.151 Safari/535.19";
		case 6:// firefox
			return "Mozilla/5.0 (Windows NT 6.1; rv:12.0) Gecko/20120403211507 Firefox/12.0";
		case 7:// ie
			return "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Trident/4.0)";
		case 8:// nokia
			return "User-Agent: Mozilla/5.0 (SymbianOS/9.1; U; [en]; Series60/3.0 NokiaE60/4.06.0) AppleWebKit/413 (KHTML, like Gecko) Safari/413";
		case 9:// safari
			return "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us) AppleWebKit/48 (like Gecko) Safari/48";
		case 10:// wp
			return "Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0";
		}
		return "";
	}
	
	static public String getEncoding(int iEncoding) {//identical
		String tmpEncoding = "AUTOSELECT";
		switch (iEncoding) {
		case 1:
			tmpEncoding = "gbk";
			break;
		case 2:
			tmpEncoding = "big5";
			break;
		case 3:
			tmpEncoding = "gb2312";
			break;
		case 4:
			tmpEncoding = "utf-8";
			break;
		case 5:
			tmpEncoding = "iso-8859-1";
			break;
		case 6:
			tmpEncoding = "ISO-2022-JP";
			break;
		case 7:
			tmpEncoding = "SHIFT_JIS";
			break;
		case 8:
			tmpEncoding = "EUC-JP";
			break;
		case 9:
			tmpEncoding = "EUC-KR";
			break;
		}
		return tmpEncoding;
	}

	static public String getName(String url) {//identical
		String readableUrl = url;
		try {
			readableUrl = URLDecoder.decode(url);// change something like http%3A%2F%2Fwww%2Ebaidu%2Ecom%3Fcid%3D%2D%5F1 to http://www.baidu.com?cid=-_1
		} catch (Exception e) {}// report crash by Elad, so catch it.
		
		if (readableUrl.endsWith("/"))
			readableUrl = readableUrl.substring(0, readableUrl.length() - 1); // such as http://m.cnbeta.com/, http://www.google.com.hk/
		
		int posQ = readableUrl.indexOf("?");
		if (posQ > 0)
			readableUrl = readableUrl.substring(0, posQ);// cut off post paras if any.

		String ss[] = readableUrl.split("/");
		String apkName = ss[ss.length - 1].toLowerCase(); // get download file
		// name
		if (apkName.contains("="))
			apkName = apkName.split("=")[apkName.split("=").length - 1];

		return apkName;
	}
	
	static public void openDownload(TitleUrl tu, Context context) {
		Intent intent = new Intent("android.intent.action.VIEW");
		
		String ext = tu.m_title.substring(tu.m_title.lastIndexOf(".")+1, tu.m_title.length());
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		String mimeType = mimeTypeMap.getMimeTypeFromExtension(ext);
		if (mimeType != null) intent.setDataAndType(Uri.parse(tu.m_url), mimeType);
		else intent.setData(Uri.parse(tu.m_url));// we can open it now
		
		util.startActivity(intent, true, context);
	}
	

}
