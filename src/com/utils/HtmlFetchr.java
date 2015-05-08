package com.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Bundle;

import com.model.ArticleElement;
import com.model.Blog;
import com.model.Column;

/**
 * ͨ���������ӵ�ַ��������ȡ��html��Դ�����ط�װ�õĶ�Ӧ��Ҫ������Ķ���
 */
public class HtmlFetchr {

	private static final String TAG = "HtmlFetchr";
	public static final String EXTRA_COLUMN_BUILD_DATE = "column_build_date";
	public static final String EXTRA_COLUMN_BLOG_NUMB = "column_blog_numb";
	public static final String EXTRA_COLUMN_READ_NUMB = "column_read_numb";
	public static final int DROP_UPDATE = 0; // ��������
	public static final int UP_LOAD = 1; // ��������
	private String mHtmlString;

	/**
	 * ����URLָ������Դ
	 * 
	 * @return ����Ϊ����byte[]
	 * */
	public byte[] getUrlBytes(String urlSpec) {

		HttpURLConnection conn = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in = null;
		try {
			URL url = new URL(urlSpec);
			conn = (HttpURLConnection) url.openConnection();
			in = conn.getInputStream();
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				// Log.i(TAG, "���Ӳ��ɹ�");
				return null;
			}
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (conn != null)
				conn.disconnect();
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return out.toByteArray();
	}

	/**
	 * ����URLָ������Դ(����getUrlBytes�����ķ���ֵbyte[]ת����String����)
	 * 
	 * @return ��������ΪString
	 */
	private String getUrl(String urlSpec) {
		String result = null;
		result = new String(getUrlBytes(urlSpec));
		return result;
	}

	/**
	 * ��ȡ����ҳ������
	 */
	public ArrayList<ArticleElement> downloadBlogPageElements(String urlSpec) {

		String htmlString = getUrl(urlSpec);
		ArrayList<ArticleElement> elements = new ArrayList<>();
		// �������ʧ��
		if (htmlString.length() == 0) {
			return null;
		}


		Document doc = Jsoup.parse(htmlString);

		// ����
		String title = doc.getElementsByClass("article_title").get(0).text();
		ArticleElement element = new ArticleElement();
		element.setTitle(title);
		elements.add(element);

		// ����
		Elements childrenEle = doc.getElementsByClass("article_content").get(0)
				.children();
		if (doc.getElementsByClass("markdown_views").size() > 0) {
			// Log.v(TAG, "markdown_views");
			childrenEle = doc.getElementsByClass("markdown_views").get(0)
					.children();
		}
		for (Element childEle : childrenEle) {

			Elements imgEles = childEle.getElementsByTag("img");

			// ͼƬ
			for (Element imgEle : imgEles) {
				if (imgEle.attr("src").equals(""))
					continue;
				element = new ArticleElement();
				element.setImageLink(imgEle.attr("src"));
				elements.add(element);
			}

			imgEles.remove();

			// ����
			Element codElement;
			if ((codElement = isCodeExist(childEle)) != null) {
				element = new ArticleElement();
				element.setCode(codElement.text());
				elements.add(element);
				continue;
			}

			if (childEle.text().equals(""))
				continue;

			// ��ͨ����
			element = new ArticleElement();
			element.setContent(childEle.outerHtml());
			elements.add(element);
		}


		return elements;
	}

	/**
	 * �жϵ�ǰ���Ƿ���ڴ�������
	 * 
	 * @param childEle
	 */
	private Element isCodeExist(Element childEle) {
		if (childEle.getElementsByClass("plain").size() > 0)
			return childEle.getElementsByClass("plain").get(0);
		if (childEle.getElementsByClass("html").size() > 0)
			return childEle.getElementsByClass("html").get(0);
		if (childEle.getElementsByClass("objc").size() > 0)
			return childEle.getElementsByClass("objc").get(0);
		if (childEle.getElementsByClass("sql").size() > 0)
			return childEle.getElementsByClass("sql").get(0);
		if (childEle.getElementsByClass("javascript").size() > 0)
			return childEle.getElementsByClass("javascript").get(0);
		if (childEle.getElementsByClass("css").size() > 0)
			return childEle.getElementsByClass("css").get(0);
		if (childEle.getElementsByClass("php").size() > 0)
			return childEle.getElementsByClass("php").get(0);
		if (childEle.getElementsByClass("csharp").size() > 0)
			return childEle.getElementsByClass("csharp").get(0);
		if (childEle.getElementsByClass("cpp").size() > 0)
			return childEle.getElementsByClass("cpp").get(0);
		if (childEle.getElementsByClass("java").size() > 0)
			return childEle.getElementsByClass("java").get(0);
		if (childEle.getElementsByClass("python").size() > 0)
			return childEle.getElementsByClass("python").get(0);
		if (childEle.getElementsByClass("ruby").size() > 0)
			return childEle.getElementsByClass("ruby").get(0);
		if (childEle.getElementsByClass("vb").size() > 0)
			return childEle.getElementsByClass("vb").get(0);
		if (childEle.getElementsByClass("delphi").size() > 0)
			return childEle.getElementsByClass("delphi").get(0);
		if (childEle.getElementsByClass("prettyprint").size() > 0)
			return childEle.getElementsByClass("prettyprint").get(0);
		return null;
	}

	/**
	 * ���ز���������������ҳ��htmlҳ�棬����ArrayList<Blog>����
	 */

	public ArrayList<Blog> downloadBlogs(ArrayList<Blog> blogs, int code,
			String urlSpec) {
		mHtmlString = getUrl(urlSpec);
		// �������ʧ��
		if (mHtmlString.length() == 0)
			return null;
		// ����htmlString
		parserBlogs(blogs, code, mHtmlString);
		return blogs;

	}

	private void parserBlogs(ArrayList<Blog> blogs, int code, String htmlString) {

		boolean isFirstRequst = false; // �Ƿ��ǵ�һ������
		if (blogs.size() == 0)
			isFirstRequst = true;
		Document doc = Jsoup.parse(htmlString);
		// ���������б����ϸ��Ϣ
		Elements units = doc.getElementsByClass("blog_list");
		int tag = 0;// ʹ�õı��
		for (int i = 0; i < units.size(); i++) {

			Element unit_ele = units.get(i);
			String title = null;
			String type = null;
			String blogurl = null;
			if (unit_ele.getElementsByTag("h1").get(0).children().size() == 1) {
				// û�����
				title = unit_ele.getElementsByTag("h1").get(0).child(0).text();// ���ͱ���
				// Log.i(TAG, "title:" + title);
				blogurl = unit_ele.getElementsByTag("h1").get(0).child(0)
						.attr("href");
				// Log.i(TAG, "blogurl:" + blogurl);
			} else {
				// �����
				type = unit_ele.getElementsByTag("h1").get(0).child(0).text();// ��������
				// Log.i(TAG, "type:" + type);
				title = unit_ele.getElementsByTag("h1").get(0).child(1).text();// ���ͱ���
				// Log.i(TAG, "title:" + title);
				blogurl = unit_ele.getElementsByTag("h1").get(0).child(1)
						.attr("href");
				// Log.i(TAG, "blogurl:" + blogurl);
			}

			String content = unit_ele.getElementsByTag("dd").get(0).text();// ���ͼ��
			// Log.i(TAG, "content:" + content);

			String author = unit_ele.getElementsByClass("fl").get(0).child(0)
					.text(); // ��������
			// Log.i(TAG, "author:" + author);

			String ago = unit_ele.getElementsByClass("fl").get(0).child(1)
					.text(); // ���ͷ���ʱ��
			// Log.i(TAG, "ago:" + ago);
			String readnumb = unit_ele.getElementsByClass("fl").get(0).child(2)
					.text(); // �Ķ�����
			// Log.i(TAG, "readnumb:" + readnumb);
			String commentnumb = unit_ele.getElementsByClass("fl").get(0)
					.child(3).text(); // ���۴���
			// Log.i(TAG, "commentnumb:" + commentnumb);

			String imageurl = unit_ele.getElementsByTag("dt").get(0).child(0)
					.child(0).attr("src");
			// Log.i(TAG, "imgUrl:" + imageurl);

			Blog blog = new Blog();
			blog.setBlogUrl(blogurl);
			blog.setBlogAuthor(author);
			blog.setBlogType(type);
			blog.setBlogTitle(title);
			blog.setBlogContent(content);
			blog.setAgo(ago);
			blog.setCommentNumb(commentnumb);
			blog.setReadNum(readnumb);
			blog.setBloggerIcon(imageurl);
			// -------------------------------------------------

			if (code == HtmlFetchr.UP_LOAD) { // �������������
				blogs.add(blog); // ���صĻ���ֱ����ӵ�β������
			} else {
				if (isFirstRequst) {// ����ǵ�һ��������£������е�blog�����η���
					blogs.add(blog);
				} else {
					// �ڶ����������
					if (blogs.get(tag).getBlogUrl().equals(blog.getBlogUrl())) {
						return; // ˵���˺�Ķ�������columns�У������ٸ���
					} else {
						blogs.add(tag, blog); // �����ڵĶ����ö�β
						++tag;
					}
				}
			}

		}

	}

	/**
	 * ���ز���������ר����ҳ��htmlҳ�棬����ArrayList<Column>����
	 * 
	 * @param code
	 *            ��ʾ�������»�����������
	 * 
	 * @param mColumns
	 * 
	 * @param mCurrentPage
	 */
	public ArrayList<Column> downloadColumns(ArrayList<Column> columns,
			int code, String urlSpec) {
		mHtmlString = getUrl(urlSpec);
		// �������ʧ��
		if (mHtmlString.length() == 0)
			return null;
		// ����htmlString
		parserColumns(columns, code, mHtmlString);
		return columns;
	}

	private void parserColumns(ArrayList<Column> columns, int code,
			String htmlString) {
		boolean isFirstRequst = false; // �Ƿ��ǵ�һ������
		if (columns.size() == 0)
			isFirstRequst = true;
		Document doc = Jsoup.parse(htmlString);
		// ����ר���б����ϸ��Ϣ
		Elements units = doc.getElementsByClass("columns_recom");
		Element unit_ele = units.get(0);
		int tag = 0;// ʹ�õı��
		for (int i = 0; i < unit_ele.children().size(); i++) {
			// ��ȡר����Ϣ
			Column column = new Column();
			Element dl_ele = unit_ele.getElementsByTag("dl").get(i);
			Element dt_ele = dl_ele.getElementsByTag("dt").get(0);
			Element dt_a_ele = dt_ele.child(0);
			String imageUrl = dt_a_ele.child(0).attr("src"); // ͼƬ����
			String owner = dt_ele.child(1).text(); // ��ӵ��id

			Element dd_ele = dl_ele.getElementsByTag("dd").get(0);
			String title = dd_ele.child(0).text(); // ר������
			String content = dd_ele.text(); // ר������˵��

			String columnUrl = dd_ele.child(0).attr("href");
			column.setColumnOwner(owner);
			column.setColumnTitle(title);
			column.setColumnContent(content);
			column.setImageUrl(imageUrl);
			column.setColumnUrl(columnUrl);
			// -------------------------------------------------

			if (code == HtmlFetchr.UP_LOAD) { // �������������
				columns.add(column); // ���صĻ���ֱ����ӵ�β������
			} else {
				if (isFirstRequst) {// ����ǵ�һ��������£������е�column�����η���
					columns.add(column);
				} else {
					// �ڶ����������
					if (columns.get(tag).getColumnUrl()
							.equals(column.getColumnUrl())) {
						return; // ˵���˺�Ķ�������columns�У������ٸ���
					} else {
						columns.add(tag, column); // �����ڵĶ����ö�β
						++tag;
					}
				}
			}

		}

	}

	/**
	 * ��ȡ���ж���ҳ
	 */
	public int downloadPages() {
		int pages;
		// ����htmlString
		pages = parserPages(mHtmlString);
		return pages;
	}

	private int parserPages(String htmlString) {
		int pages;
		Document doc = Jsoup.parse(htmlString);
		// �������ж���ҳ
		Element page_ele = doc.getElementsByClass("page_nav").get(0).child(0);
		String page_text = page_ele.text();
		String result = page_text.substring(page_text.indexOf("��") + 1,
				page_text.indexOf("ҳ"));
		pages = Integer.parseInt(result);
		// Log.v(TAG, "" + pages);
		return pages;
	}

	/**
	 * ���ػ�ȡ����ר��ҳ�����Ϣ������ʱ�䡢����������������������ص�Bundle���󽫱�����Щֵ
	 */
	public Bundle downloadColumnInfo(String urlSpec) {
		String htmlString = getUrl(urlSpec);
		// �������ʧ��
		if (htmlString.length() == 0)
			return null;
		Bundle result = new Bundle();
		parserColumnInfo(htmlString, result); // ����html
		return result;
	}

	private void parserColumnInfo(String htmlString, Bundle result) {
		Document doc = Jsoup.parse(htmlString);
		Element unit_ele = doc.getElementsByClass("box_1").get(0);
		Element ul_ele = unit_ele.getElementsByTag("ul").get(0);
		String buildDate = ul_ele.child(1).text();
		String blogNumb = ul_ele.child(2).text();
		String readNumb = ul_ele.child(3).text();

		result.putString(EXTRA_COLUMN_BUILD_DATE, buildDate);
		result.putString(EXTRA_COLUMN_BLOG_NUMB, blogNumb);
		result.putString(EXTRA_COLUMN_READ_NUMB, readNumb);

	}

	/**
	 * ���ز������ض�ר���в����б�
	 */
	public ArrayList<Blog> downloadColumnBlogs(String urlSpec) {
		ArrayList<Blog> blogs = new ArrayList<Blog>();
		String htmlString = getUrl(urlSpec);
		// �������ʧ��
		if (htmlString.length() == 0)
			return null;
		parserColumnBlogs(htmlString, blogs);
		return blogs;
	}

	private void parserColumnBlogs(String htmlString, ArrayList<Blog> blogs) {
		Document doc = Jsoup.parse(htmlString);
		Elements units = doc.getElementsByClass("blog_list"); // class=blog_list�����нڵ�
		for (int i = 0; i < units.size(); i++) {
			Element unit_ele = units.get(i);
			String title = null;
			String type = null;
			String blogurl = null;

			if (unit_ele.getElementsByTag("h1").get(0).children().size() == 1) {
				// û�����
				title = unit_ele.getElementsByTag("h1").get(0).child(0).text();// ���ͱ���
				// Log.i(TAG, "title:" + title);
				blogurl = unit_ele.getElementsByTag("h1").get(0).child(0)
						.attr("href");
			} else {
				// �����
				type = unit_ele.getElementsByTag("h1").get(0).child(0).text();// ��������
				// Log.i(TAG, "type:" + type);
				title = unit_ele.getElementsByTag("h1").get(0).child(1).text();// ���ͱ���
				// Log.i(TAG, "title:" + title);
				blogurl = unit_ele.getElementsByTag("h1").get(0).child(1)
						.attr("href");
			}

			String content = unit_ele.getElementsByTag("p").get(0).text();// ���ͼ��
			// Log.i(TAG, "content:" + content);
			String ago = unit_ele.getElementsByClass("fl").get(0).child(1)
					.text(); // ���ͷ���ʱ��
			// Log.i(TAG, "ago:" + ago);
			String readnumb = unit_ele.getElementsByClass("fl").get(0).child(2)
					.text(); // �Ķ�����
			// Log.i(TAG, "readnumb:" + readnumb);
			String commentnumb = unit_ele.getElementsByClass("fl").get(0)
					.child(3).text(); // ���۴���
			// Log.i(TAG, "commentnumb:" + commentnumb);

			Blog blog = new Blog();
			blog.setBlogUrl(blogurl);
			blog.setBlogType(type);
			blog.setBlogTitle(title);
			blog.setBlogContent(content);
			blog.setAgo(ago);
			blog.setCommentNumb(commentnumb);
			blog.setReadNum(readnumb);
			blogs.add(blog);
		}
	}

	/**
	 * ���ز������ҵĲ���htmlҳ�棬����ArrayList<Blog>����
	 */

	public ArrayList<Blog> downloadMyBlogs(ArrayList<Blog> blogs, int code,
			String urlSpec) {
		mHtmlString = getUrl(urlSpec);
		// �������ʧ��
		if (mHtmlString.length() == 0)
			return null;
		// ����htmlString
		parserMyBlogs(blogs, code, mHtmlString);
		return blogs;

	}

	private void parserMyBlogs(ArrayList<Blog> blogs, int code,
			String htmlString) {

		boolean isFirstRequst = false; // �Ƿ��ǵ�һ������
		if (blogs.size() == 0)
			isFirstRequst = true;
		Document doc = Jsoup.parse(htmlString);
		// ���������б����ϸ��Ϣ
		Elements units = doc.getElementsByClass("list_item");
		// Log.v(TAG, "units's size=" + units.size());
		int tag = 0;// ʹ�õı��
		for (int i = 0; i < units.size(); i++) {

			Element unit_ele = units.get(i);
			String title = unit_ele.getElementsByClass("link_title").get(0)
					.child(0).text();
			String content = unit_ele.getElementsByClass("article_description")
					.get(0).text();
			String blogurl = unit_ele.getElementsByClass("link_title").get(0)
					.child(0).attr("href");
			String ago = unit_ele.getElementsByClass("link_postdate").get(0)
					.text();
			String readnumb = unit_ele.getElementsByClass("link_view").get(0)
					.text();
			String commentnumb = unit_ele.getElementsByClass("link_comments")
					.get(0).text();

			Blog blog = new Blog();
			blog.setBlogUrl("http://blog.csdn.net/" + blogurl);
			blog.setBlogTitle(title);
			blog.setBlogContent(content);
			blog.setAgo(ago);
			blog.setCommentNumb(commentnumb);
			blog.setReadNum(readnumb);
			// -------------------------------------------------

			if (code == HtmlFetchr.UP_LOAD) { // �������������
				blogs.add(blog); // ���صĻ���ֱ����ӵ�β������
			} else {
				if (isFirstRequst) {// ����ǵ�һ��������£������е�blog�����η���
					blogs.add(blog);
				} else {
					// �ڶ����������
					if (blogs.get(tag).getBlogUrl().equals(blog.getBlogUrl())) {
						return; // ˵���˺�Ķ�������columns�У������ٸ���
					} else {
						blogs.add(tag, blog); // �����ڵĶ����ö�β
						++tag;
					}
				}
			}

		}
	}

	/**
	 * ��ȡ�ҵĲ��͹��ж���ҳ
	 */
	public int downloadMyBlogPages() {
		int pages;
		// ����htmlString
		pages = parserMyBlogPages(mHtmlString);
		return pages;
	}

	private int parserMyBlogPages(String htmlString) {
		int pages;
		Document doc = Jsoup.parse(htmlString);
		// �������ж���ҳ
		Element page_ele = doc.getElementsByClass("pagelist").get(0).child(0);
		String page_text = page_ele.text();
		String result = page_text.substring(page_text.indexOf("��") + 1,
				page_text.indexOf("ҳ"));
		pages = Integer.parseInt(result);
		// Log.v(TAG, "" + pages);
		return pages;
	}

}
