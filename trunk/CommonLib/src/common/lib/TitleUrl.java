package common.lib;

public class TitleUrl {
	public String m_title;
	public String m_url;
	public String m_site;

	public TitleUrl(String title, String url, String site) {
		if (title != null)
			m_title = title;
		else
			m_title = url;
		m_url = url;
		m_site = site;
	}
}
