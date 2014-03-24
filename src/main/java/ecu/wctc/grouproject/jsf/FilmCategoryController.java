package ecu.wctc.grouproject.jsf;

import ecu.wctc.groupproject.entity.FilmCategory;
import ecu.wctc.grouproject.jsf.util.JsfUtil;
import ecu.wctc.grouproject.jsf.util.PaginationHelper;
import com.mycompany.groupproject.FilmCategoryFacade;

import java.io.Serializable;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;

@Named("filmCategoryController")
@SessionScoped
public class FilmCategoryController implements Serializable {

    private FilmCategory current;
    private DataModel items = null;
    @EJB
    private com.mycompany.groupproject.FilmCategoryFacade ejbFacade;
    private PaginationHelper pagination;
    private int selectedItemIndex;

    public FilmCategoryController() {
    }

    public FilmCategory getSelected() {
        if (current == null) {
            current = new FilmCategory();
            current.setFilmCategoryPK(new ecu.wctc.groupproject.entity.FilmCategoryPK());
            selectedItemIndex = -1;
        }
        return current;
    }

    private FilmCategoryFacade getFacade() {
        return ejbFacade;
    }

    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getFacade().count();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getFacade().findRange(new int[]{getPageFirstItem(), getPageFirstItem() + getPageSize()}));
                }
            };
        }
        return pagination;
    }

    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (FilmCategory) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new FilmCategory();
        current.setFilmCategoryPK(new ecu.wctc.groupproject.entity.FilmCategoryPK());
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            current.getFilmCategoryPK().setCategoryId(current.getCategory().getCategoryId());
            current.getFilmCategoryPK().setFilmId(current.getFilm().getFilmId());
            getFacade().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("FilmCategoryCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (FilmCategory) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    public String update() {
        try {
            current.getFilmCategoryPK().setCategoryId(current.getCategory().getCategoryId());
            current.getFilmCategoryPK().setFilmId(current.getFilm().getFilmId());
            getFacade().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("FilmCategoryUpdated"));
            return "View";
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String destroy() {
        current = (FilmCategory) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getFacade().remove(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("FilmCategoryDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getFacade().count();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getFacade().findRange(new int[]{selectedItemIndex, selectedItemIndex + 1}).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(ejbFacade.findAll(), true);
    }

    public FilmCategory getFilmCategory(ecu.wctc.groupproject.entity.FilmCategoryPK id) {
        return ejbFacade.find(id);
    }

    @FacesConverter(forClass = FilmCategory.class)
    public static class FilmCategoryControllerConverter implements Converter {

        private static final String SEPARATOR = "#";
        private static final String SEPARATOR_ESCAPED = "\\#";

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            FilmCategoryController controller = (FilmCategoryController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "filmCategoryController");
            return controller.getFilmCategory(getKey(value));
        }

        ecu.wctc.groupproject.entity.FilmCategoryPK getKey(String value) {
            ecu.wctc.groupproject.entity.FilmCategoryPK key;
            String values[] = value.split(SEPARATOR_ESCAPED);
            key = new ecu.wctc.groupproject.entity.FilmCategoryPK();
            key.setFilmId(Short.parseShort(values[0]));
            key.setCategoryId(Short.parseShort(values[1]));
            return key;
        }

        String getStringKey(ecu.wctc.groupproject.entity.FilmCategoryPK value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value.getFilmId());
            sb.append(SEPARATOR);
            sb.append(value.getCategoryId());
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof FilmCategory) {
                FilmCategory o = (FilmCategory) object;
                return getStringKey(o.getFilmCategoryPK());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + FilmCategory.class.getName());
            }
        }

    }

}
