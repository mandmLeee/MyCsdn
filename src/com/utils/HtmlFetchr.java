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
 * 通过给定链接地址，解析获取的html资源，返回封装好的对应的要求请求的对象
 */
public class HtmlFetchr {

	private static final String TAG = "HtmlFetchr";
	public static final String EXTRA_COLUMN_BUILD_DATE = "column_build_date";
	public static final String EXTRA_COLUMN_BLOG_NUMB = "column_blog_numb";
	public static final String EXTRA_COLUMN_READ_NUMB = "column_read_numb";
	public static final int DROP_UPDATE = 0; // 上拉更新
	public static final int UP_LOAD = 1; // 下拉加载
	private String mHtmlString;

	/**
	 * 下载URL指定的资源
	 * 
	 * @return 返回为类型byte[]
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
				// Log.i(TAG, "连接不成功");
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
	 * 下载URL指定的资源(即将getUrlBytes方法的返回值byte[]转换成String类型)
	 * 
	 * @return 返回类型为String
	 */
	private String getUrl(String urlSpec) {
		String result = null;
		result = new String(getUrlBytes(urlSpec));
		return result;
	}

	/**
	 * 获取博客页面内容
	 */
	public ArrayList<ArticleElement> downloadBlogPageElements(String urlSpec) {

		String htmlString = getUrl(urlSpec);
		ArrayList<ArticleElement> elements = new ArrayList<>();
		// 如果访问失败
		if (htmlString.length() == 0) {
			return null;
		}


		Document doc = Jsoup.parse(htmlString);

		// 标题
		String title = doc.getElementsByClass("article_title").get(0).text();
		ArticleElement element = new ArticleElement();
		element.setTitle(title);
		elements.add(element);

		// 内容
		Elements childrenEle = doc.getElementsByClass("article_content").get(0)
				.children();
		if (doc.getElementsByClass("markdown_views").size() > 0) {
			// Log.v(TAG, "markdown_views");
			childrenEle = doc.getElementsByClass("markdown_views").get(0)
					.children();
		}
		for (Element childEle : childrenEle) {

			Elements imgEles = childEle.getElementsByTag("img");

			// 图片
			for (Element imgEle : imgEles) {
				if (imgEle.attr("src").equals(""))
					continue;
				element = new ArticleElement();
				element.setImageLink(imgEle.attr("src"));
				elements.add(element);
			}

			imgEles.remove();

			// 代码
			Element codElement;
			if ((codElement = isCodeExist(childEle)) != null) {
				element = new ArticleElement();
				element.setCode(codElement.text());
				elements.add(element);
				continue;
			}

			if (childEle.text().equals(""))
				continue;

			// 普通内容
			element = new ArticleElement();
			element.setContent(childEle.outerHtml());
			elements.add(element);
		}


		return elements;
	}

	/**
	 * 判断当前树是否存在代码子树
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
	 * 下载并解析热门文章主页的html页面，返回ArrayList<Blog>对象
	 */

	public ArrayList<Blog> downloadBlogs(ArrayList<Blog> blogs, int code,
			String urlSpec) {
		mHtmlString = getUrl(urlSpec);
		// 如果访问失败
		if (mHtmlString.length() == 0)
			return null;
		// 解析htmlString
		parserBlogs(blogs, code, mHtmlString);
		return blogs;

	}

	private void parserBlogs(ArrayList<Blog> blogs, int code, String htmlString) {

		boolean isFirstRequst = false; // 是否是第一次请求
		if (blogs.size() == 0)
			isFirstRequst = true;
		Document doc = Jsoup.parse(htmlString);
		// 解析博客列表的详细信息
		Elements units = doc.getElementsByClass("blog_list");
		int tag = 0;// 使用的标记
		for (int i = 0; i < units.size(); i++) {

			Element unit_ele = units.get(i);
			String title = null;
			String type = null;
			String blogurl = null;
			if (unit_ele.getElementsByTag("h1").get(0).children().size() == 1) {
				// 没有类别
				title = unit_ele.getElementsByTag("h1").get(0).child(0).text();// 博客标题
				// Log.i(TAG, "title:" + title);
				blogurl = unit_ele.getElementsByTag("h1").get(0).child(0)
						.attr("href");
				// Log.i(TAG, "blogurl:" + blogurl);
			} else {
				// 有类别
				type = unit_ele.getElementsByTag("h1").get(0).child(0).text();// 博客类型
				// Log.i(TAG, "type:" + type);
				title = unit_ele.getElementsByTag("h1").get(0).child(1).text();// 博客标题
				// Log.i(TAG, "title:" + title);
				blogurl = unit_ele.getElementsByTag("h1").get(0).child(1)
						.attr("href");
				// Log.i(TAG, "blogurl:" + blogurl);
			}

			String content = unit_ele.getElementsByTag("dd").get(0).text();// 博客简介
			// Log.i(TAG, "content:" + content);

			String author = unit_ele.getElementsByClass("fl").get(0).child(0)
					.text(); // 博客作者
			// Log.i(TAG, "author:" + author);

			String ago = unit_ele.getElementsByClass("fl").get(0).child(1)
					.text(); // 博客发布时间
			// Log.i(TAG, "ago:" + ago);
			String readnumb = unit_ele.getElementsByClass("fl").get(0).child(2)
					.text(); // 阅读次数
			// Log.i(TAG, "readnumb:" + readnumb);
			String commentnumb = unit_ele.getElementsByClass("fl").get(0)
					.child(3).text(); // 评论次数
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

			if (code == HtmlFetchr.UP_LOAD) { // 如果是上拉加载
				blogs.add(blog); // 加载的话，直接添加到尾部即可
			} else {
				if (isFirstRequst) {// 如果是第一次请求更新，将所有的blog都依次放入
					blogs.add(blog);
				} else {
					// 第二次请求更新
					if (blogs.get(tag).getBlogUrl().equals(blog.getBlogUrl())) {
						return; // 说明此后的都存在于columns中，不需再更新
					} else {
						blogs.add(tag, blog); // 不存在的都添置队尾
						++tag;
					}
				}
			}

		}

	}

	/**
	 * 下载并解析博客专栏主页的html页面，返回ArrayList<Column>对象
	 * 
	 * @param code
	 *            表示下拉更新或者上拉加载
	 * 
	 * @param mColumns
	 * 
	 * @param mCurrentPage
	 */
	public ArrayList<Column> downloadColumns(ArrayList<Column> columns,
			int code, String urlSpec) {
		mHtmlString = getUrl(urlSpec);
		// 如果访问失败
		if (mHtmlString.length() == 0)
			return null;
		// 解析htmlString
		parserColumns(columns, code, mHtmlString);
		return columns;
	}

	private void parserColumns(ArrayList<Column> columns, int code,
			String htmlString) {
		boolean isFirstRequst = false; // 是否是第一次请求
		if (columns.size() == 0)
			isFirstRequst = true;
		Document doc = Jsoup.parse(htmlString);
		// 解析专栏列表的详细信息
		Elements units = doc.getElementsByClass("columns_recom");
		Element unit_ele = units.get(0);
		int tag = 0;// 使用的标记
		for (int i = 0; i < unit_ele.children().size(); i++) {
			// 获取专栏信息
			Column column = new Column();
			Element dl_ele = unit_ele.getElementsByTag("dl").get(i);
			Element dt_ele = dl_ele.getElementsByTag("dt").get(0);
			Element dt_a_ele = dt_ele.child(0);
			String imageUrl = dt_a_ele.child(0).attr("src"); // 图片链接
			String owner = dt_ele.child(1).text(); // 　拥有id

			Element dd_ele = dl_ele.getElementsByTag("dd").get(0);
			String title = dd_ele.child(0).text(); // 专栏名称
			String content = dd_ele.text(); // 专题内容说明

			String columnUrl = dd_ele.child(0).attr("href");
			column.setColumnOwner(owner);
			column.setColumnTitle(title);
			column.setColumnContent(content);
			column.setImageUrl(imageUrl);
			column.setColumnUrl(columnUrl);
			// -------------------------------------------------

			if (code == HtmlFetchr.UP_LOAD) { // 如果是上拉加载
				columns.add(column); // 加载的话，直接添加到尾部即可
			} else {
				if (isFirstRequst) {// 如果是第一次请求更新，将所有的column都依次放入
					columns.add(column);
				} else {
					// 第二次请求更新
					if (columns.get(tag).getColumnUrl()
							.equals(column.getColumnUrl())) {
						return; // 说明此后的都存在于columns中，不需再更新
					} else {
						columns.add(tag, column); // 不存在的都添置队尾
						++tag;
					}
				}
			}

		}

	}

	/**
	 * 获取共有多少页
	 */
	public int downloadPages() {
		int pages;
		// 解析htmlString
		pages = parserPages(mHtmlString);
		return pages;
	}

	private int parserPages(String htmlString) {
		int pages;
		Document doc = Jsoup.parse(htmlString);
		// 解析共有多少页
		Element page_ele = doc.getElementsByClass("page_nav").get(0).child(0);
		String page_text = page_ele.text();
		String result = page_text.substring(page_text.indexOf("共") + 1,
				page_text.indexOf("页"));
		pages = Integer.parseInt(result);
		// Log.v(TAG, "" + pages);
		return pages;
	}

	/**
	 * 下载获取单独专栏页面的信息：创建时间、浏览次数、文章数量。返回的Bundle对象将保存这些值
	 */
	public Bundle downloadColumnInfo(String urlSpec) {
		String htmlString = getUrl(urlSpec);
		// 如果访问失败
		if (htmlString.length() == 0)
			return null;
		Bundle result = new Bundle();
		parserColumnInfo(htmlString, result); // 解析html
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
	 * 下载并解析特定专栏中博文列表
	 */
	public ArrayList<Blog> downloadColumnBlogs(String urlSpec) {
		ArrayList<Blog> blogs = new ArrayList<Blog>();
		String htmlString = getUrl(urlSpec);
		// 如果访问失败
		if (htmlString.length() == 0)
			return null;
		parserColumnBlogs(htmlString, blogs);
		return blogs;
	}

	private void parserColumnBlogs(String htmlString, ArrayList<Blog> blogs) {
		Document doc = Jsoup.parse(htmlString);
		Elements units = doc.getElementsByClass("blog_list"); // class=blog_list的所有节点
		for (int i = 0; i < units.size(); i++) {
			Element unit_ele = units.get(i);
			String title = null;
			String type = null;
			String blogurl = null;

			if (unit_ele.getElementsByTag("h1").get(0).children().size() == 1) {
				// 没有类别
				title = unit_ele.getElementsByTag("h1").get(0).child(0).text();// 博客标题
				// Log.i(TAG, "title:" + title);
				blogurl = unit_ele.getElementsByTag("h1").get(0).child(0)
						.attr("href");
			} else {
				// 有类别
				type = unit_ele.getElementsByTag("h1").get(0).child(0).text();// 博客类型
				// Log.i(TAG, "type:" + type);
				title = unit_ele.getElementsByTag("h1").get(0).child(1).text();// 博客标题
				// Log.i(TAG, "title:" + title);
				blogurl = unit_ele.getElementsByTag("h1").get(0).child(1)
						.attr("href");
			}

			String content = unit_ele.getElementsByTag("p").get(0).text();// 博客简介
			// Log.i(TAG, "content:" + content);
			String ago = unit_ele.getElementsByClass("fl").get(0).child(1)
					.text(); // 博客发布时间
			// Log.i(TAG, "ago:" + ago);
			String readnumb = unit_ele.getElementsByClass("fl").get(0).child(2)
					.text(); // 阅读次数
			// Log.i(TAG, "readnumb:" + readnumb);
			String commentnumb = unit_ele.getElementsByClass("fl").get(0)
					.child(3).text(); // 评论次数
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
	 * 下载并解析我的博客html页面，返回ArrayList<Blog>对象
	 */

	public ArrayList<Blog> downloadMyBlogs(ArrayList<Blog> blogs, int code,
			String urlSpec) {
		mHtmlString = getUrl(urlSpec);
		// 如果访问失败
		if (mHtmlString.length() == 0)
			return null;
		// 解析htmlString
		parserMyBlogs(blogs, code, mHtmlString);
		return blogs;

	}

	private void parserMyBlogs(ArrayList<Blog> blogs, int code,
			String htmlString) {

		boolean isFirstRequst = false; // 是否是第一次请求
		if (blogs.size() == 0)
			isFirstRequst = true;
		Document doc = Jsoup.parse(htmlString);
		// 解析博客列表的详细信息
		Elements units = doc.getElementsByClass("list_item");
		// Log.v(TAG, "units's size=" + units.size());
		int tag = 0;// 使用的标记
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

			if (code == HtmlFetchr.UP_LOAD) { // 如果是上拉加载
				blogs.add(blog); // 加载的话，直接添加到尾部即可
			} else {
				if (isFirstRequst) {// 如果是第一次请求更新，将所有的blog都依次放入
					blogs.add(blog);
				} else {
					// 第二次请求更新
					if (blogs.get(tag).getBlogUrl().equals(blog.getBlogUrl())) {
						return; // 说明此后的都存在于columns中，不需再更新
					} else {
						blogs.add(tag, blog); // 不存在的都添置队尾
						++tag;
					}
				}
			}

		}
	}

	/**
	 * 获取我的博客共有多少页
	 */
	public int downloadMyBlogPages() {
		int pages;
		// 解析htmlString
		pages = parserMyBlogPages(mHtmlString);
		return pages;
	}

	private int parserMyBlogPages(String htmlString) {
		int pages;
		Document doc = Jsoup.parse(htmlString);
		// 解析共有多少页
		Element page_ele = doc.getElementsByClass("pagelist").get(0).child(0);
		String page_text = page_ele.text();
		String result = page_text.substring(page_text.indexOf("共") + 1,
				page_text.indexOf("页"));
		pages = Integer.parseInt(result);
		// Log.v(TAG, "" + pages);
		return pages;
	}

}
