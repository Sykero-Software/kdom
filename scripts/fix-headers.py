"""Restore the JDOM licence header of every translated kdom source verbatim from upstream jdom2.

Usage: python3 -I scripts/fix-headers.py [--check] <kdom repo root> <jdom2 checkout (tag JDOM-2.0.6.1)>
(--check: report only, byte-level; exit summary lists every translated file's state.)

For each git-tracked .kt/.java file under src/ whose path maps to an upstream jdom2 source (core/src/java/org/jdom2/ or
test/src/java/org/jdom2/, `.jvm`/`.ios` suffixes dropped) that begins with a `/*--` header block, the file's own leading
`/* ... */` block is replaced by the upstream block, byte for byte (trailing white space included), with the single kdom
translation line inserted as the last text line of the block (before the closing blank line and ` */`). The rest of the
file is untouched. Some upstream test files put the block after the `package` line (within the first 10 lines); their
kdom copies get it at the same place (package line, blank line, block). An explicit map covers translated files whose
upstream source has another name or place. Translated files whose upstream has no licence block keep the one-line
`/* <translation line> */` comment as their first line.
"""
import os
import re
import subprocess
import sys

TL = ' Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software.'
EXTRA = {
    # kdom file (relative to src/...) -> upstream file (relative to the jdom2 checkout)
    'commonMain/kotlin/fi/sykero/kdom/input/TreeBuildingHandler.kt': 'core/src/java/org/jdom2/input/sax/SAXHandler.java',
    # split out of XMLOutputter (the OutputStream overloads)
    'jvmMain/kotlin/fi/sykero/kdom/output/XMLOutputterJvm.kt': 'core/src/java/org/jdom2/output/XMLOutputter.java',
    # raw-instance tests cut from TestText/TestComment/TestProcessingInstruction/TestAttribute/TestElement; the licensed
    # sources among them share TestElement's block
    'jvmTest/java/fi/sykero/kdom/test/cases/RawInstanceTest.java': 'test/src/java/org/jdom2/test/cases/TestElement.java',
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


def header_block(ut):
    """The upstream licence block (from `/*--` to its closing ` */`) with the translation line inserted, and its layout."""
    if ut.startswith('/*--'):
        at, layout = 0, 'first'
    else:
        # some upstream tests put the block after the package line: look in the first 10 lines
        at = ut.find('\n/*--', 0, len('\n'.join(ut.split('\n')[:10])))
        if at < 0:
            return None, None
        at += 1
        layout = 'after-package'
    close = ut.find('\n */', at)
    block = ut[at:close].replace('org.jdom2', 'fi.sykero.kdom')  # ends with the closing blank line (may hold spaces)
    last_nl = block.rfind('\n')
    return block[:last_nl + 1] + TL + '\n' + block[last_nl + 1:] + '\n */', layout


ONE_LINER = '/*' + TL + ' */\n'


def expected(t, new_header, layout):
    """The kdom file text with its licence block in place (everything else unchanged)."""
    if t.startswith(ONE_LINER):
        t = t[len(ONE_LINER):]
    if layout == 'first':
        if t.startswith('/*'):
            return new_header + t[t.find('*/') + 2:]
        return new_header + '\n\n' + t
    pkg = t.find('package ')
    eol = t.find('\n', pkg)
    rest = t[eol + 1:].lstrip('\n')
    if rest.startswith('/*--'):
        rest = rest[rest.find('\n */') + 4:].lstrip('\n')
    return t[:eol + 1] + '\n' + new_header + '\n\n' + rest


def main():
    check = '--check' in sys.argv
    args = [a for a in sys.argv[1:] if a != '--check']
    root, up = args[0], args[1]
    files = [f for f in subprocess.check_output(['git', '-C', root, 'ls-files', 'src'], text=True).split()
             if f.endswith(('.kt', '.java'))]
    changed = 0
    stats = {}
    for f in files:
        path = os.path.join(root, f)
        with open(path, encoding='utf-8', newline='') as fh:
            t = fh.read()
        u = upstream(up, f)
        if u is None:
            kind = 'new (no upstream), translation lines=%d' % t.count(TL)
            stats[kind] = stats.get(kind, 0) + 1
            continue
        with open(u, encoding='utf-8', newline='') as fh:
            ut = fh.read()
        new_header, layout = header_block(ut)
        if new_header is None:
            ok = t.startswith(ONE_LINER) and t.count(TL) == 1
            kind = 'upstream without licence block: one-line translation comment ' + ('ok' if ok else 'MISSING')
            stats[kind] = stats.get(kind, 0) + 1
            continue
        nt = expected(t, new_header, layout)
        ok = nt == t and t.count(TL) == 1
        kind = 'licence block %s: %s' % (layout, 'verbatim' if ok else 'DIFFERS')
        stats[kind] = stats.get(kind, 0) + 1
        if nt != t and not check:
            with open(path, 'w', encoding='utf-8', newline='') as fh:
                fh.write(nt)
            changed += 1
            print('fixed', f)
        elif not ok:
            print('differs', f)
    for k in sorted(stats):
        print('%4d  %s' % (stats[k], k))
    print('changed', changed)


main()
