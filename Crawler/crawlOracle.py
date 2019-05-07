import requests
import io, re
from bs4 import BeautifulSoup, NavigableString, Tag, Comment

windows_textpath = "Documents\\Oracle\\"
oracle_url = 'https://docs.oracle.com/javase/tutorial/'
oracle_java_url = 'https://docs.oracle.com/javase/tutorial/reallybigindex.html'

textType = "text"
codeType = "code"

link_prefix = 'Java Programming/'
link_prefix_length = len(link_prefix)

othersCount = 0


def get_links_for_crawling(WebUrl):
    links = {}
    url = WebUrl
    code = requests.get(url)
    plain = code.text
    s = BeautifulSoup(plain, "html.parser")
    content = s.findAll('a')
    for link in content:
        name = link.text
        name = '' if name is None else name.encode('utf-8')
        name.replace(" ", "_")
        href = link.get('href')
        if "/" in name:
            name = name.replace('/', '-')
        if href is not None:
            links[name] = oracle_url + href
    print(links)
    print(len(links))
    return links


def crawl_links(links):
    for name, url in links.iteritems():
        text = crawl_site_link(url, name)


def write_to_file(name, content):
    path = windows_textpath
    name = re.sub('[^A-Za-z0-9. _\-]+', '', name)
    f = io.open(path + 'Oracle_' + name + '.txt', 'w', encoding='utf8')
    f.write(content)
    f.close()


def crawl_site_link(url, name):
    if name is '':
        return
    code = requests.get(url)
    plain = code.text
    soup = BeautifulSoup(plain, "html.parser")

    content = soup.find('div', {'id': 'PageContent'})
    if content is None:
        return

    for element in content(text=lambda text: isinstance(text, Comment)):
        element.extract()

    content = content.text
    # print(content)
    write_to_file(name, content)


links = get_links_for_crawling(oracle_java_url)
crawl_links(links)

# crawl_site_link(links["Working with URLs"], "Working with URLs")
# crawl_site_link("https://en.wikibooks.org/wiki/Java_Programming/About_This_Book")
