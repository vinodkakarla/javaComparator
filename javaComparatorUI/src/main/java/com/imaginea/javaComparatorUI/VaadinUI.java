package com.imaginea.javaComparatorUI;

import info.bliki.wiki.tags.code.JavaCodeFilter;
import info.bliki.wiki.tags.code.SourceCodeFormatter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.gson.Gson;
import com.imaginea.javaStructuralComparator.domain.ComparisonResult;
import com.imaginea.javaStructuralComparator.domain.Import;
import com.imaginea.javaStructuralComparator.domain.Type;
import com.imaginea.javaStructuralComparator.domain.uiNode.Package;
import com.imaginea.javaStructuralComparator.repo.ComparatorImpl;
import com.imaginea.javaStructuralComparator.repo.ComparatorUtil;
import com.vaadin.annotations.Theme;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Main UI class
 */
@Theme("MyVaadinTheme")
@SuppressWarnings("serial")
public class VaadinUI extends UI {

	private String javaCodeFormatterTag1 = "<pre class=\"java\" style=\"border: 1px solid #b4d0dc; background-color: #ecf8ff;\">";
	private String javaCodeFormatterTag2 = "</pre>";

	public static void main(String[] args) throws IOException {
		String json = new VaadinUI().readFile("e:/dummy.json");
		Gson gson = new Gson();
		ComparisonResult cResult = gson.fromJson(json, ComparisonResult.class);
		System.out.println(cResult.getImports().size());
	}

	@Override
	protected void init(VaadinRequest request) {

		String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/resources/TestJavaFiles/";
		String fileA = basepath + request.getParameter("fileA");
		String fileB = basepath + request.getParameter("fileB");

		// initTest();
		initUI(fileA, fileB);
		// tester();
	}

	@SuppressWarnings("deprecation")
	private void initUI(String fileA, String fileB) {

		final HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		setContent(splitPanel);

		final HorizontalSplitPanel subSplitPanel = new HorizontalSplitPanel();

		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);

		// String json = null;
		// try {
		// json = new VaadinUI().readFile("E:/dummy.json");
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }
		// Gson gson = new Gson();
		// ComparisonResult cResult = gson.fromJson(json,
		// ComparisonResult.class);
		ComparatorImpl comparator = new ComparatorImpl();
		ComparisonResult cResult = comparator.compare(fileA, fileB);
		final Tree tree = new Tree("ComparisonStructure");
		constructTree(tree, cResult);

		setStyleGenerator(tree);

		tree.addListener(new ItemClickEvent.ItemClickListener() {

			public void itemClick(ItemClickEvent event) {
				System.out.println("Clling...");
				Label[] panels = new Label[2];
				Object obj = event.getItemId();

				if (obj.getClass() == com.imaginea.javaStructuralComparator.domain.uiNode.Import.class) {
					panels = constructImports4Display(((com.imaginea.javaStructuralComparator.domain.uiNode.Import) obj).getImprt());
				} else if (obj.getClass() == com.imaginea.javaStructuralComparator.domain.uiNode.Type.class) {
					panels = constructTypes4Display(((com.imaginea.javaStructuralComparator.domain.uiNode.Type) obj).getType());
				} else if (obj.getClass() == com.imaginea.javaStructuralComparator.domain.uiNode.Package.class) {
					panels = constructPackage4Display(((com.imaginea.javaStructuralComparator.domain.uiNode.Package) obj).getPkg());
				} else {
					panels[0] = getJavaLabel(obj.toString());
					panels[1] = getJavaLabel(obj.toString());
				}
				subSplitPanel.removeAllComponents();
				layout.addComponent(panels[0]);
				subSplitPanel.addComponent(panels[0]);
				subSplitPanel.addComponent(panels[1]);
			}
		});

		VerticalLayout leftLayout = new VerticalLayout();
		splitPanel.addComponent(leftLayout);
		splitPanel.addComponent(subSplitPanel);
		leftLayout.addComponent(tree);

		AbsoluteLayout aLayout = new AbsoluteLayout();
		aLayout.setWidth("400px");
		aLayout.setHeight("250px");

		// A component with coordinates for its top-left corner
		Label text = new Label("Red nodes are present in both the files with difference");
		text.setStyleName("diff");
		Label text1 = new Label("Blue nodes are present in first file");
		text1.setStyleName("actual");
		Label text2 = new Label("Green nodes are present in second file");
		text2.setStyleName("expected");
		aLayout.addComponent(text, "bottom: 150px;");
		aLayout.addComponent(text1, "bottom: 130px;");
		aLayout.addComponent(text2, "bottom: 110px;");

		leftLayout.addComponent(aLayout);
	}

	private void setStyleGenerator(final Tree tree) {
		tree.setItemStyleGenerator(new Tree.ItemStyleGenerator() {
			@Override
			public String getStyle(Tree source, Object itemId) {
				short diff = getDifference(itemId);
				if (diff == ComparatorUtil.EXPECTEDDEFAULT)
					return "expected";
				if (diff == ComparatorUtil.MODIFIEDDEFAULT)
					return "diff";
				if (diff == ComparatorUtil.ACTUALDEFAULT)
					return "actual";
				else
					return "noDiff";
			}
		});

		// tree.requestRepaint();
	}

	private short getDifference(Object itemId) {
		if (com.imaginea.javaStructuralComparator.domain.uiNode.Package.class == itemId.getClass())
			return ((com.imaginea.javaStructuralComparator.domain.uiNode.Package) itemId).getPkg().getDiff();
		if (com.imaginea.javaStructuralComparator.domain.uiNode.Import.class == itemId.getClass())
			return ((com.imaginea.javaStructuralComparator.domain.uiNode.Import) itemId).getImprt().getDiff();
		if (com.imaginea.javaStructuralComparator.domain.uiNode.Type.class == itemId.getClass())
			return ((com.imaginea.javaStructuralComparator.domain.uiNode.Type) itemId).getType().getDiff();

		return 0;
	}

	private Label getJavaLabel(String readFile) {
		SourceCodeFormatter f = new JavaCodeFilter();
		String result = null;
		result = f.filter(readFile);
		Label richText = new Label(javaCodeFormatterTag1 + result + javaCodeFormatterTag2);
		richText.setContentMode(ContentMode.HTML);
		return richText;
	}

	void constructTree(Tree tree, ComparisonResult result) {
		tree.setStyleName("newlabel");

		Label l = new Label("Package");
		tree.addItem(l);
		com.imaginea.javaStructuralComparator.domain.uiNode.Package pkgObj = new Package(result.getPkg());
		tree.addItem(pkgObj);
		tree.setParent(pkgObj, l);
		tree.setChildrenAllowed(pkgObj, false);

		l = new Label("Imports");
		tree.addItem(l);
		List<Import> imports = result.getImports();
		constructTree(tree, l, imports);

		l = new Label("Types");
		tree.addItem(l);
		List<Type> types = result.getTypes();
		constructTypesTree(tree, l, types);

	}

	void constructTree(Tree tree, Object parent, List<Import> imports) {
		for (Import imprt : imports) {
			if (imprt.getDiff() == ComparatorUtil.NOMODIFICATION)
				continue;
			com.imaginea.javaStructuralComparator.domain.uiNode.Import obj = new com.imaginea.javaStructuralComparator.domain.uiNode.Import(
					imprt);

			tree.addItem(obj);
			tree.setParent(obj, parent);
			tree.setChildrenAllowed(obj, false);
		}
	}

	void constructTypesTree(Tree tree, Object parent, List<Type> types) {
		for (Type type : types) {
			if (type.getDiff() == ComparatorUtil.NOMODIFICATION)
				continue;
			com.imaginea.javaStructuralComparator.domain.uiNode.Type obj = new com.imaginea.javaStructuralComparator.domain.uiNode.Type(
					type);
			tree.addItem(obj);
			tree.setParent(obj, parent);
			// tree.setStyleName("change");
			if (type.getCommonChilds().size() > 0)
				constructTypesTree(tree, obj, type.getCommonChilds());
			else
				tree.setChildrenAllowed(obj, false);
		}
	}

	private String readFile(String fileName) throws IOException {
		FileInputStream fis = new FileInputStream(fileName);
		StringBuilder inputStringBuilder = new StringBuilder();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
		String line = bufferedReader.readLine();
		while (line != null) {
			inputStringBuilder.append(line);
			inputStringBuilder.append('\n');
			line = bufferedReader.readLine();
		}
		return inputStringBuilder.toString();

	}

	private Label[] constructTypes4Display(Type type) {
		Short diff = type.getDiff();
		Label[] labels = new Label[2];
		if (diff != ComparatorUtil.EXPECTEDDEFAULT) {
			labels[0] = getJavaLabel(type.getAbstractDeclarations(0).getCompleteNodeValue());
			if (diff == ComparatorUtil.ACTUALDEFAULT) {
				Label lab = getJavaLabel("Nothing to Display");
				lab.setStyleName("diff");
				labels[1] = lab;
			}
		}
		if (diff != ComparatorUtil.ACTUALDEFAULT) {
			labels[1] = getJavaLabel(type.getAbstractDeclarations(1).getCompleteNodeValue());
			if (diff == ComparatorUtil.EXPECTEDDEFAULT) {
				Label lab = getJavaLabel("Nothing to Display");
				lab.setStyleName("diff");
				labels[0] = lab;
			}
		}
		return labels;
	}

	protected Label[] constructPackage4Display(com.imaginea.javaStructuralComparator.domain.Package pkg) {
		Short diff = pkg.getDiff();
		Label[] labels = new Label[2];
		if (diff != ComparatorUtil.EXPECTEDDEFAULT) {
			labels[0] = getJavaLabel(pkg.getLines(0).getValue());
			if (diff == ComparatorUtil.ACTUALDEFAULT) {
				labels[1] = getJavaLabel("Nothing to Display");
			}
		}
		if (diff != ComparatorUtil.ACTUALDEFAULT) {
			labels[1] = getJavaLabel(pkg.getLines(1).getValue());
			if (diff == ComparatorUtil.EXPECTEDDEFAULT) {
				labels[0] = getJavaLabel("Nothing to Display");
			}
		}
		return labels;
	}

	private Label[] constructImports4Display(Import imprt) {
		Short diff = imprt.getDiff();
		Label[] labels = new Label[2];
		if (diff != ComparatorUtil.EXPECTEDDEFAULT) {
			labels[0] = getJavaLabel(imprt.getLine(0).getValue());
			if (diff == ComparatorUtil.ACTUALDEFAULT) {
				labels[1] = getJavaLabel("Nothing to Display");
			}
		}
		if (diff != ComparatorUtil.ACTUALDEFAULT) {
			labels[1] = getJavaLabel(imprt.getLine(1).getValue());
			if (diff == ComparatorUtil.EXPECTEDDEFAULT) {
				labels[0] = getJavaLabel("Nothing to Display");
			}
		}
		return labels;
	}

}