package testsUnitaires;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import personnel.*;
import java.time.LocalDate;

class testEmploye {

    private GestionPersonnel gestionPersonnel;
    private Ligue ligue;
    private Employe employe;

    @BeforeEach
    void setUp() throws SauvegardeImpossible, DateInvalideException, DateIncoherenteException {
        // Initialisation des objets pour les tests
        gestionPersonnel = GestionPersonnel.getGestionPersonnel();
        ligue = gestionPersonnel.addLigue("Fléchettes");
        employe = ligue.addEmploye("Dupont", "Jean", "j.dupont@gmail.com", "password", null, null);
    }

    // Test des setters
    @Test
    void testSetNom() {
        employe.setNom("Martin");
        assertEquals("Martin", employe.getNom(), "Le nom de l'employé doit être mis à jour.");
    }

    @Test
    void testSetPrenom() {
        employe.setPrenom("Alice");
        assertEquals("Alice", employe.getPrenom(), "Le prénom de l'employé doit être mis à jour.");
    }

    @Test
    void testSetMail() {
        employe.setMail("a.martin@gmail.com");
        assertEquals("a.martin@gmail.com", employe.getMail(), "L'adresse e-mail de l'employé doit être mise à jour.");
    }

    @Test
    void testSetPassword() {
        employe.setPassword("newpassword");
        assertTrue(employe.checkPassword("newpassword"), "Le mot de passe de l'employé doit être mis à jour.");
    }

    @Test
    void testSetDateArriveeValide() throws DateInvalideException, DateIncoherenteException {
        LocalDate nouvelleDateArrivee = LocalDate.now().plusDays(5);
        employe.setDateArrivee(nouvelleDateArrivee);
        assertEquals(nouvelleDateArrivee, employe.getDateArrivee(), "La date d'arrivée doit être mise à jour.");
    }

    @Test
    void testSetDateArriveeInvalide() {
        LocalDate datePassee = LocalDate.now().minusDays(1);
        assertThrows(DateInvalideException.class, () -> {
            employe.setDateArrivee(datePassee);
        }, "Une DateInvalideException doit être levée si la date d'arrivée est dans le passé.");
    }

    @Test
    void testSetDateDepartValide() throws DateInvalideException, DateIncoherenteException {
        LocalDate nouvelleDateDepart = LocalDate.now().plusDays(10);
        employe.setDateDepart(nouvelleDateDepart);
        assertEquals(nouvelleDateDepart, employe.getDateDepart(), "La date de départ doit être mise à jour.");
    }

    @Test
    void testSetDateDepartIncoherente() throws DateInvalideException, DateIncoherenteException {
        LocalDate dateArrivee = LocalDate.now().plusDays(5);
        employe.setDateArrivee(dateArrivee);

        LocalDate dateDepartIncoherente = LocalDate.now().plusDays(3);
        assertThrows(DateIncoherenteException.class, () -> {
            employe.setDateDepart(dateDepartIncoherente);
        }, "Une DateIncoherenteException doit être levée si la date de départ est avant la date d'arrivée.");
    }

    // Test de la suppression d'un employé
    @Test
    void testRemoveEmploye() {
        employe.remove();
        assertFalse(ligue.getEmployes().contains(employe), "L'employé doit être supprimé de la ligue.");
    }

    // Test de la modification de l'administrateur d'une ligue
    @Test
    void testSetAdministrateur() throws SauvegardeImpossible, DateInvalideException, DateIncoherenteException {
        Employe nouvelAdmin = ligue.addEmploye("Admin", "Test", "admin@test.com", "admin", null, null);
        ligue.setAdministrateur(nouvelAdmin);

        assertEquals(nouvelAdmin, ligue.getAdministrateur(), "Le nouvel administrateur doit être défini.");
        assertTrue(nouvelAdmin.estAdmin(ligue), "L'employé doit être reconnu comme administrateur de la ligue.");
    }

    // Test de la suppression de l'administrateur d'une ligue
    @Test
    void testRemoveAdministrateur() throws SauvegardeImpossible, DateInvalideException, DateIncoherenteException {
        Employe admin = ligue.addEmploye("Admin", "Test", "admin@test.com", "admin", null, null);
        ligue.setAdministrateur(admin);
        //verifier que l'employer est bien defini
        assertNotNull(ligue.getAdministrateur(), "L'administrateur doit être défini.");
        admin.remove();
        assertNull(ligue.getAdministrateur(), "L'administrateur doit être supprimé de la ligue.");
    }
}
