package ecu.wctc.grouproject.jsf;

import ecu.wctc.groupproject.entity.Actor;
import ecu.wctc.grouproject.jsf.util.JsfUtil;
import ecu.wctc.grouproject.jsf.util.PaginationHelper;
import com.mycompany.groupproject.ActorFacade;

import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

@Named("actorController")
@SessionScoped
public class ActorController implements Serializable {

    private Actor current;
    @Inject
    private com.mycompany.groupproject.ActorFacade ejbFacade;
    private List<Actor> actors;
    
    public ActorController() {
    }
    
    @PostConstruct
    private void init(){
        setActors(ejbFacade.findAll());
    }

    public Actor getActor(java.lang.Short id) {
        return ejbFacade.find(id);
    }

    /**
     * @return the actors
     */
    public List<Actor> getActors() {
        return actors;
    }

    /**
     * @param actors the actors to set
     */
    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }

    @FacesConverter(forClass = Actor.class)
    public static class ActorControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            ActorController controller = (ActorController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "actorController");
            return controller.getActor(getKey(value));
        }

        java.lang.Short getKey(String value) {
            java.lang.Short key;
            key = Short.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Short value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Actor) {
                Actor o = (Actor) object;
                return getStringKey(o.getActorId());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Actor.class.getName());
            }
        }

    }

}
