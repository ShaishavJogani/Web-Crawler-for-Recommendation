import requests
import io, re
from bs4 import BeautifulSoup, NavigableString, Tag, Comment

windows_textpath = "Documents\\Raw\\"
windows_codepath = "Documents\\IndexData\\Code\\"
wikibooks_url = 'https://en.wikibooks.org'
wikibooks_java_url = 'https://en.wikibooks.org/wiki/Java_Programming'

textType = "text"
codeType = "code"

link_prefix = 'Java Programming/'
link_prefix_length = len(link_prefix)


def get_links_for_crawling(WebUrl):
    links = {}
    url = WebUrl
    code = requests.get(url)
    plain = code.text
    s = BeautifulSoup(plain, "html.parser")
    content = s.find('div', {'class': 'mw-parser-output'})
    for link in content.findAll('a'):
        title = link.get('title')
        title = '' if title is None else title.encode('utf-8')
        if link_prefix in title:
            name = title[link_prefix_length:]
            name.replace(" ", "_")
            href = link.get('href')
            if "/" in name:
                temp = str(name).split("/")
                name = temp[len(temp) - 1]
            links[name] = wikibooks_url + href
    print(links)
    return links


def crawl_links(links):
    # links = {}
    for name, url in links.iteritems():
        text = crawl_site_link(url, name)
        # file = open(windows_path + name + '.txt', 'w')
        # file.write(text)
        # file.close()


def write_to_file(name, title, type, content):
    path = "Documents\\"
    if type is codeType:
        path = windows_codepath
    else:
        path = windows_textpath
    name = re.sub('[^A-Za-z0-9. _\-]+', '', name)
    title = re.sub('[^A-Za-z0-9. _\-]+', '', title)
    f = io.open(path + name + '_' + title + '_' + type + '.txt', 'w', encoding='utf8')
    f.write(content)
    f.close()
    # TODO: uncomment above 3 lines


def crawl_site_link(url, name):
    code = requests.get(url)
    plain = code.text
    soup = BeautifulSoup(plain, "html.parser")
    content = soup.find('div', {'id': 'mw-content-text'})
    for element in content(text=lambda text: isinstance(text, Comment)):
        element.extract()
    for header in content.find_all('h2'):
        nextNode = header
        title = nextNode.text
        if '[edit]' in title:
            title = title[:-len('[edit]')]
        content = ""
        while True:
            nextNode = nextNode.nextSibling
            if nextNode is None:
                break
            if isinstance(nextNode, NavigableString):
                content += nextNode.strip() + '\n'
            if isinstance(nextNode, Tag):
                if nextNode.name == "h2":
                    break
                code = nextNode.find('div', {'class': 'mw-highlight'})
                if code is not None:
                    for span in code.find_all("span", {'class': 'lineno'}):
                        span.decompose()
                    # print code
                    ananta = nextNode.find_all("b")
                    if ananta is not None and len(ananta) > 0:
                        # print(ananta)
                        code_heading = ananta[0].text
                        if "Code section " in code_heading:
                            code_heading = code_heading[len("Code section "):]
                        elif "Code listing " in code_heading:
                            code_heading = code_heading[len("Code listing "):]
                        else:
                            code_heading = "Other"
                        code_heading = re.sub('[^A-Za-z0-9. ]+', '', code_heading)
                        # print(code_heading)
                        write_to_file(name, code_heading, codeType, code.text)

                else:
                    content += nextNode.get_text(strip=True).strip() + '\n'
        write_to_file(name, title, textType, content)


links = get_links_for_crawling(wikibooks_java_url)
links.pop('Print version')
crawl_links(links)

# crawl_site_link(links["Scope"], "Scope")
# crawl_site_link("https://en.wikibooks.org/wiki/Java_Programming/About_This_Book")
