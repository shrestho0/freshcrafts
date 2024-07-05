import re
import requests

with open("./src/lib/ui/css/all.css") as f:

    content = f.read()

    file_to_write = open("./src/lib/ui/css/all_remote_removed.css", "w")

    # find @font-face { in contetn
    exp = "@font-face\s*{[^}]*}"
    h_exp = 'url\("([^"]*)"\)'
    count_2, count_1 = 0, 0
    for m in re.findall(exp, content):
        str0, str1 = str(m), ""
        if str0.count("url") == 2:
            # print(str0, len(str0), str0.count("url"))
            str1 = str0

        if str0 in content and str0.count("url") == 2:
            count_2 += 1

            for old_url in re.findall(h_exp, str1):
                ## download from old_url
                font_name = old_url.split("/")[-1]
                ## save to ./fonts/

                r = requests.get(old_url, allow_redirects=True)
                fl = f"./src/lib/ui/css/fonts/{font_name}"

                open(fl, "wb").write(r.content)

                print(font_name)
                str1 = str1.replace(old_url, f"/fonts/{font_name}")

            print("str0 found")
        else:
            count_1 += 1

        content = content.replace(str0, str1)

    print(count_1 + count_2, count_1, count_2)

    file_to_write.write(content)
