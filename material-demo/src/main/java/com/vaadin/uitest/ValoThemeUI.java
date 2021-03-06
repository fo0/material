/*
 * Copyright 2000-2016 Vaadin Ltd.
 *
 * Modifications Copyright 2017 appreciated
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.vaadin.uitest;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Viewport;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.*;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Container;
import com.vaadin.v7.data.Container.Hierarchical;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.HierarchicalContainer;
import com.vaadin.v7.data.util.IndexedContainer;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

@Theme("demo")
@Title("Valo Theme Test")
@PreserveOnRefresh
@Viewport("user-scalable=no,initial-scale=1.0")
public class ValoThemeUI extends UI {

    static final String CAPTION_PROPERTY = "caption";
    static final String DESCRIPTION_PROPERTY = "description";
    static final String ICON_PROPERTY = "icon";
    static final String INDEX_PROPERTY = "index";
    static Handler actionHandler = new Handler() {
        private final Action ACTION_ONE = new Action("Action One");
        private final Action ACTION_TWO = new Action("Action Two");
        private final Action ACTION_THREE = new Action("Action Three");
        private final Action[] ACTIONS = new Action[]{ACTION_ONE, ACTION_TWO,
                ACTION_THREE};

        @Override
        public void handleAction(Action action, Object sender, Object target) {
            Notification.show(action.getCaption());
        }
        @Override
        public Action[] getActions(Object target, Object sender) {
            return ACTIONS;
        }
    };
    ValoMenuLayout root = new ValoMenuLayout();
    ComponentContainer viewDisplay = root.getContentContainer();
    CssLayout menu = new CssLayout();
    CssLayout menuItemsLayout = new CssLayout();
    private boolean testMode = false;
    private TestIcon testIcon = new TestIcon(100);
    private Navigator navigator;
    private LinkedHashMap<String, String> menuItems = new LinkedHashMap<>();

    {
        menu.setId("testMenu");
    }

    static boolean isTestMode() {
        return ((ValoThemeUI) getCurrent()).testMode;
    }

    static Handler getActionHandler() {
        return actionHandler;
    }

    @SuppressWarnings("unchecked")
    static Container generateContainer(final int size,
                                       final boolean hierarchical) {
        TestIcon testIcon = new TestIcon(90);
        IndexedContainer container = hierarchical ? new HierarchicalContainer()
                : new IndexedContainer();
        StringGenerator sg = new StringGenerator();
        container.addContainerProperty(CAPTION_PROPERTY, String.class, null);
        container.addContainerProperty(ICON_PROPERTY, Resource.class, null);
        container.addContainerProperty(INDEX_PROPERTY, Integer.class, null);
        container.addContainerProperty(DESCRIPTION_PROPERTY, String.class,
                null);
        for (int i = 1; i < size + 1; i++) {
            Item item = container.addItem(i);
            item.getItemProperty(CAPTION_PROPERTY)
                    .setValue(sg.nextString(true) + " " + sg.nextString(false));
            item.getItemProperty(INDEX_PROPERTY).setValue(i);
            item.getItemProperty(DESCRIPTION_PROPERTY)
                    .setValue(sg.nextString(true) + " " + sg.nextString(false)
                            + " " + sg.nextString(false));
            item.getItemProperty(ICON_PROPERTY).setValue(testIcon.get());
        }
        container.getItem(container.getIdByIndex(0))
                .getItemProperty(ICON_PROPERTY).setValue(testIcon.get());

        if (hierarchical) {
            for (int i = 1; i < size + 1; i++) {
                for (int j = 1; j < 5; j++) {
                    String id = i + " -> " + j;
                    Item child = container.addItem(id);
                    child.getItemProperty(CAPTION_PROPERTY).setValue(
                            sg.nextString(true) + " " + sg.nextString(false));
                    child.getItemProperty(ICON_PROPERTY)
                            .setValue(testIcon.get());
                    ((Hierarchical) container).setParent(id, i);

                    for (int k = 1; k < 6; k++) {
                        String id2 = id + " -> " + k;
                        child = container.addItem(id2);
                        child.getItemProperty(CAPTION_PROPERTY)
                                .setValue(sg.nextString(true) + " "
                                        + sg.nextString(false));
                        child.getItemProperty(ICON_PROPERTY)
                                .setValue(testIcon.get());
                        ((Hierarchical) container).setParent(id2, id);

                        for (int l = 1; l < 5; l++) {
                            String id3 = id2 + " -> " + l;
                            child = container.addItem(id3);
                            child.getItemProperty(CAPTION_PROPERTY)
                                    .setValue(sg.nextString(true) + " "
                                            + sg.nextString(false));
                            child.getItemProperty(ICON_PROPERTY)
                                    .setValue(testIcon.get());
                            ((Hierarchical) container).setParent(id3, id2);
                        }
                    }
                }
            }
        }
        return container;
    }

    @Override
    protected void init(VaadinRequest request) {
        if (request.getParameter("test") != null) {
            testMode = true;

            if (browserCantRenderFontsConsistently()) {
                getPage().getStyles()
                        .add(".v-app.v-app.v-app {font-family: Sans-Serif;}");
            }
        }

        if (getPage().getWebBrowser().isIE()
                && getPage().getWebBrowser().getBrowserMajorVersion() == 9) {
            menu.setWidth("320px");
        }

        if (!testMode) {
            Responsive.makeResponsive(this);
        }

        getPage().setTitle("Material Theme Test");
        setContent(root);
        root.setWidth("100%");

        root.addMenu(buildMenu());

        navigator = new Navigator(this, viewDisplay);

        navigator.addView("common", CommonParts.class);
        navigator.addView("labels", Labels.class);
        navigator.addView("buttons-and-links", ButtonsAndLinks.class);
        navigator.addView("textfields", TextFields.class);
        navigator.addView("datefields", DateFields.class);
        navigator.addView("comboboxes", ComboBoxes.class);
        navigator.addView("checkboxes", CheckBoxes.class);
        navigator.addView("sliders", Sliders.class);
        navigator.addView("menubars", MenuBars.class);
        navigator.addView("panels-no-caption", PanelsNoCaptionTest.class);
        navigator.addView("panels", Panels.class);
        navigator.addView("tables", Tables.class);
        navigator.addView("splitpanels", SplitPanels.class);
        navigator.addView("tabs", Tabsheets.class);
        navigator.addView("grids", Grid.class);
        navigator.addView("trees", Trees.class);
        navigator.addView("accordions", Accordions.class);
        navigator.addView("colorpickers", ColorPickers.class);
        navigator.addView("selects", NativeSelects.class);
        navigator.addView("calendar", CalendarTest.class);
        navigator.addView("forms", Forms.class);
        navigator.addView("popupviews", PopupViews.class);
        navigator.addView("dragging", Dragging.class);
        navigator.addView("material", Material.class); // Added own section key
        navigator.addView("uploads", Custom.class); // Added own section key

        String f = Page.getCurrent().getUriFragment();
        if (f == null || f.equals("")) {
            navigator.navigateTo("common");
        }

        navigator.addViewChangeListener(new ViewChangeListener() {

            @Override
            public boolean beforeViewChange(ViewChangeEvent event) {
                return true;
            }

            @Override
            public void afterViewChange(ViewChangeEvent event) {
                for (Iterator<Component> it = menuItemsLayout.iterator(); it
                        .hasNext(); ) {
                    it.next().removeStyleName("selected");
                }
                for (Entry<String, String> item : menuItems.entrySet()) {
                    if (event.getViewName().equals(item.getKey())) {
                        for (Iterator<Component> it = menuItemsLayout
                                .iterator(); it.hasNext(); ) {
                            Component c = it.next();
                            if (c.getCaption() != null && c.getCaption()
                                    .startsWith(item.getValue())) {
                                c.addStyleName("selected");
                                break;
                            }
                        }
                        break;
                    }
                }
                menu.removeStyleName("valo-menu-visible");
            }
        });

    }

    private boolean browserCantRenderFontsConsistently() {
        // PhantomJS renders font correctly about 50% of the time, so
        // disable it to have consistent screenshots
        // https://github.com/ariya/phantomjs/issues/10592

        return getPage().getWebBrowser().getBrowserApplication()
                .contains("PhantomJS");
    }

    Component buildTestMenu() {
        CssLayout menu = new CssLayout();
        menu.addStyleName(ValoTheme.MENU_PART_LARGE_ICONS);

        Label logo = new Label("Va");
        logo.setSizeUndefined();
        logo.setPrimaryStyleName(ValoTheme.MENU_LOGO);
        menu.addComponent(logo);

        Button b = new Button(
                "Reference <span class=\"valo-menu-badge\">3</span>");
        b.setIcon(VaadinIcons.LIST);
        b.setPrimaryStyleName(ValoTheme.MENU_ITEM);
        b.addStyleName("selected");
        b.setCaptionAsHtml(true);
        menu.addComponent(b);

        b = new Button("API");
        b.setIcon(VaadinIcons.BOOK);
        b.setPrimaryStyleName(ValoTheme.MENU_ITEM);
        menu.addComponent(b);

        b = new Button("Examples <span class=\"valo-menu-badge\">12</span>");
        b.setIcon(VaadinIcons.TABLE);
        b.setPrimaryStyleName(ValoTheme.MENU_ITEM);
        b.setHtmlContentAllowed(true);
        menu.addComponent(b);

        return menu;
    }

    CssLayout buildMenu() {
        // Add items
        menuItems.put("common", "Common UI Elements");
        menuItems.put("material", "Material Elements"); // Added own section view
        menuItems.put("labels", "Labels");
        menuItems.put("buttons-and-links", "Buttons & Links");
        menuItems.put("textfields", "Text Fields");
        menuItems.put("datefields", "Date Fields");
        menuItems.put("comboboxes", "Combo Boxes");
        menuItems.put("selects", "Selects");
        menuItems.put("checkboxes", "Check Boxes & Option Groups");
        menuItems.put("sliders", "Sliders & Progress Bars");
        menuItems.put("colorpickers", "Color Pickers");
        menuItems.put("menubars", "Menu Bars");
        menuItems.put("trees", "Trees");
        menuItems.put("tables", "Tables");
        menuItems.put("grids", "Grids");
        menuItems.put("dragging", "Drag and Drop");
        menuItems.put("panels", "Panels");
        menuItems.put("panels-no-caption", "Panels No Caption");
        menuItems.put("splitpanels", "Split Panels");
        menuItems.put("tabs", "Tabs");
        menuItems.put("accordions", "Accordions");
        menuItems.put("popupviews", "Popup Views");
        menuItems.put("calendar", "Calendar");
        menuItems.put("forms", "Forms");
        menuItems.put("uploads", "Uploads");

        HorizontalLayout top = new HorizontalLayout();
        top.setWidth("100%");
        top.setSpacing(false);
        top.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        top.addStyleName(ValoTheme.MENU_TITLE);
        menu.addComponent(top);

        Button showMenu = new Button("Menu", new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                if (menu.getStyleName().contains("valo-menu-visible")) {
                    menu.removeStyleName("valo-menu-visible");
                } else {
                    menu.addStyleName("valo-menu-visible");
                }
            }
        });
        showMenu.addStyleName(ValoTheme.BUTTON_PRIMARY);
        showMenu.addStyleName(ValoTheme.BUTTON_SMALL);
        showMenu.addStyleName("valo-menu-toggle");
        showMenu.setIcon(VaadinIcons.LIST);
        menu.addComponent(showMenu);

        Label title = new Label("<h3>Vaadin <strong>Material</strong> Theme</h3>", // Added custom Title
                ContentMode.HTML);
        title.setSizeUndefined();
        top.addComponent(title);
        top.setExpandRatio(title, 1);

        MenuBar settings = new MenuBar();
        settings.addStyleName("user-menu");
        StringGenerator sg = new StringGenerator();
        MenuItem settingsItem = settings.addItem(
                sg.nextString(true) + " " + sg.nextString(true)
                        + sg.nextString(false),
                new ThemeResource("../tests-valo/img/profile-pic-300px.jpg"),
                null);
        settingsItem.addItem("Edit Profile", null);
        settingsItem.addItem("Preferences", null);
        settingsItem.addSeparator();
        settingsItem.addItem("Sign Out", null);
        menu.addComponent(settings);

        menuItemsLayout.setPrimaryStyleName("valo-menuitems");
        menu.addComponent(menuItemsLayout);

        Label label = null;
        int count = -1;
        for (final Entry<String, String> item : menuItems.entrySet()) {
            if (item.getKey().equals("labels")) {
                label = new Label("Components", ContentMode.HTML);
                label.setPrimaryStyleName(ValoTheme.MENU_SUBTITLE);
                label.addStyleName(ValoTheme.LABEL_H4);
                label.setSizeUndefined();
                menuItemsLayout.addComponent(label);
            }
            if (item.getKey().equals("panels")) {
                label.setValue(
                        label.getValue() + " <span class=\"valo-menu-badge\">"
                                + count + "</span>");
                count = 0;
                label = new Label("Containers", ContentMode.HTML);
                label.setPrimaryStyleName(ValoTheme.MENU_SUBTITLE);
                label.addStyleName(ValoTheme.LABEL_H4);
                label.setSizeUndefined();
                menuItemsLayout.addComponent(label);
            }
            if (item.getKey().equals("calendar")) {
                label.setValue(
                        label.getValue() + " <span class=\"valo-menu-badge\">"
                                + count + "</span>");
                count = 0;
                label = new Label("Other", ContentMode.HTML);
                label.setPrimaryStyleName(ValoTheme.MENU_SUBTITLE);
                label.addStyleName(ValoTheme.LABEL_H4);
                label.setSizeUndefined();
                menuItemsLayout.addComponent(label);
            }
            Button b = new Button(item.getValue(), new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    navigator.navigateTo(item.getKey());
                }
            });
            if (count == 2) {
                b.setCaption(b.getCaption()
                        + " <span class=\"valo-menu-badge\">123</span>");
            }
            b.setCaptionAsHtml(true);
            b.setPrimaryStyleName(ValoTheme.MENU_ITEM);
            b.setIcon(testIcon.get());
            menuItemsLayout.addComponent(b);
            count++;
        }
        label.setValue(label.getValue() + " <span class=\"valo-menu-badge\">"
                + count + "</span>");

        return menu;
    }
}
