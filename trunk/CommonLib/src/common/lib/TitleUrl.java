package common.lib;

public class TitleUrl {
	String m_title;
	String m_url;
	String m_site;

	TitleUrl(String title, String url, String site) {
		if (title != null)
			m_title = title;
		else
			m_title = url;
		m_url = url;
		m_site = site;
	}
}
