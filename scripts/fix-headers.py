"""Restore the JDOM licence header of every translated kdom source verbatim from upstream jdom2.

Usage: python3 -I scripts/fix-headers.py <kdom repo root> <jdom2 checkout (tag JDOM-2.0.6.1)>

For each git-tracked .kt/.java file under src/ whose path maps to an upstream jdom2 source (core/src/java/org/jdom2/ or
test/src/java/org/jdom2/, `.jvm`/`.ios` suffixes dropped) that begins with a `/*--` header block, the file's own leading
`/* ... */` block is replaced by the upstream block, byte for byte (trailing white space included), with the single kdom
translation line inserted as the last text line of the block (before the closing blank line and ` */`). The rest of the
file is untouched. An explicit map covers translated files whose upstream source has another name or place.
Files without an upstream header are reported and left alone.
"""
import os
import re
import subprocess
import sys

TL = ' Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software.'
EXTRA = {
    # kdom file (relative to src/...) -> upstream file (relative to the jdom2 checkout)
    'commonMain/kotlin/fi/sykero/kdom/input/TreeBuildingHandler.kt': 'core/src/java/org/jdom2/input/sax/SAXHandler.java',
}


def upstream(root_up, f):
    rel_src = f[len('src/'):]
    if rel_src in EXTRA:
        return os.path.join(root_up, EXTRA[rel_src])
    m = re.match(r'src/(\w+?)(Main|Test)/(?:kotlin|java)/fi/sykero/kdom/(.*)\.(kt|java)$', f)
    if not m:
        return None
    rel = re.sub(r'\.(jvm|ios)$', '', m.group(3))
    for base in ('core/src/java/org/jdom2/', 'test/src/java/org/jdom2/'):
        p = os.path.join(root_up, base + rel + '.java')
        if os.path.exists(p):
            return p
    return None


def main():
    root, up = sys.argv[1], sys.argv[2]
    files = [f for f in subprocess.check_output(['git', '-C', root, 'ls-files', 'src'], text=True).split()
             if f.endswith(('.kt', '.java'))]
    changed = 0
    for f in files:
        u = upstream(up, f)
        if u is None:
            continue
        with open(u, encoding='utf-8', newline='') as fh:
            ut = fh.read()
        if not ut.startswith('/*--'):
            continue
        close = ut.find('\n */')
        block = ut[:close].replace('org.jdom2', 'fi.sykero.kdom')  # ends with the closing blank line (may hold spaces)
        last_nl = block.rfind('\n')
        new_header = block[:last_nl + 1] + TL + '\n' + block[last_nl + 1:] + '\n */'
        path = os.path.join(root, f)
        with open(path, encoding='utf-8', newline='') as fh:
            t = fh.read()
        if t.startswith('/*'):
            end = t.find('*/') + 2
            rest = t[end:]
        else:
            rest = '\n\n' + t
        nt = new_header + rest
        if nt != t:
            with open(path, 'w', encoding='utf-8', newline='') as fh:
                fh.write(nt)
            changed += 1
            print('fixed', f)
    print('changed', changed)


main()
