package testsUnitaires;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import personnel.*;
import java.time.LocalDate;

class EmployeTest {
   
	GestionPersonnel gestionPersonnel = GestionPersonnel.getGestionPersonnel();

    @Test
    void testDatesValides() throws SauvegardeImpossible {
        Ligue ligue = gestionPersonnel.addLigue("TestLigue");
        Employe employe = ligue.addEmploye("Nom", "Prenom", "test@example.com", "password", LocalDate.parse("2024-03-15"), LocalDate.parse("2025-03-15"));
        assertEquals(LocalDate.parse("2024-03-15"), employe.getDateArrivee());
        assertEquals(LocalDate.parse("2025-03-15"), employe.getDateDepart());
    }

    @Test
    void testDatesVides() throws SauvegardeImpossible {
        Ligue ligue = gestionPersonnel.addLigue("TestLigue");
        Employe employe = ligue.addEmploye("Nom", "Prenom", "test@example.com", "password", null, null); // Passer null pour les dates vides
        assertNull(employe.getDateArrivee());
        assertNull(employe.getDateDepart());
    }

  
}