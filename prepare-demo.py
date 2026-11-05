import json, re, xml.etree.ElementTree as ET
from pathlib import Path
root=Path(__file__).parent
def vector(source,target):
    svg=ET.parse(root/source).getroot()
    box=svg.attrib['viewBox'].split()
    out=ET.Element('vector',{'xmlns:android':'http://schemas.android.com/apk/res/android','android:width':'128dp','android:height':'128dp','android:viewportWidth':box[2],'android:viewportHeight':box[3]})
    def visit(node,parent,fill='#000000'):
        tag=node.tag.split('}')[-1]
        fill=node.get('fill',fill)
        if node.get('class')=='st0': fill='#FFFFFF'
        if node.get('class')=='st1': fill='#E4002B'
        if tag=='g':
            attrs={}
            if node.get('transform'):
                nums=re.findall(r'-?\d+(?:\.\d+)?',node.get('transform'))
                assert node.get('transform').startswith('translate(')
                attrs={'android:translateX':nums[0],'android:translateY':nums[1]}
            parent=ET.SubElement(parent,'group',attrs)
        d=None
        if tag=='path': d=node.get('d')
        if tag=='polygon':
            nums=re.findall(r'-?\d+(?:\.\d+)?',node.get('points'))
            d='M'+' L'.join(nums[i]+','+nums[i+1] for i in range(0,len(nums),2))+' Z'
        if tag=='rect':
            x,y,w,h=[float(node.get(k,'0')) for k in ['x','y','width','height']]
            d=f'M{x},{y} h{w} v{h} h{-w} Z'
        if d: ET.SubElement(parent,'path',{'android:fillColor':fill,'android:pathData':d})
        for child in node: visit(child,parent,fill)
    visit(svg,out)
    ET.indent(out)
    ET.ElementTree(out).write(root/target,encoding='utf-8',xml_declaration=True)
vector('kfc-source.svg','app/src/main/res/drawable/logo_kfc.xml')
vector('circlek-source.svg','app/src/main/res/drawable/logo_circlek.xml')
p=root/'app/src/main/assets/demo-catalog.json'
d=json.loads(p.read_text(encoding='utf-8'))
d['restaurants']['demo-r2']['logo']='logo_kfc'
d['restaurants']['demo-r3']['logo']='logo_circlek'
d['restaurants']['demo-r4']['name']='Highlands Coffee'
names=['Pizza phô mai (mẫu)','Burger bò (mẫu)','Nước ngọt có ga (mẫu)','Mì xào hải sản (mẫu)']
for i,name in enumerate(names,1): d['foods'][f'demo-f{i}']['name']=name
for key,image in {'demo-f10':'ic_coke','demo-f11':'ic_banh_mi','demo-f13':'ic_noodle','demo-f15':'ic_milk_tea','demo-f16':'ic_milk_tea'}.items(): d['foods'][key]['image']=image
d['options']['demo-o9']['optionName']='Thạch trái cây'
p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
