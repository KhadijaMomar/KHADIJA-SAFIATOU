
--
-- Database: `maisonligue`
--

-- --------------------------------------------------------

--
-- Table structure for table `appartenir`
--

CREATE TABLE `appartenir` (
  `idutilisateur` int NOT NULL,
  `idligue` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `avoirunrole`
--

CREATE TABLE `avoirunrole` (
  `idutilisateur` int NOT NULL,
  `idrole` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `ligue`
--

CREATE TABLE `ligue` (
  `idligue` int NOT NULL,
  `nomligue` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `rôle`
--

CREATE TABLE `rôle` (
  `idrole` int NOT NULL,
  `nomrole` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `utilisateur`
--

CREATE TABLE `utilisateur` (
  `idutilisateur` int NOT NULL,
  `nom` varchar(50) DEFAULT NULL,
  `prénom` varchar(50) DEFAULT NULL,
  `email` varchar(50) DEFAULT NULL,
  `mot_depasse` varchar(50) DEFAULT NULL,
  `typeutilisateur` varchar(50) DEFAULT NULL,
  `datearrivée` date DEFAULT NULL,
  `datedepart` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `appartenir`
--
ALTER TABLE `appartenir`
  ADD PRIMARY KEY (`idutilisateur`,`idligue`),
  ADD KEY `idligue` (`idligue`);

--
-- Indexes for table `avoirunrole`
--
ALTER TABLE `avoirunrole`
  ADD PRIMARY KEY (`idutilisateur`,`idrole`),
  ADD KEY `idrole` (`idrole`);

--
-- Indexes for table `ligue`
--
ALTER TABLE `ligue`
  ADD PRIMARY KEY (`idligue`);

--
-- Indexes for table `rôle`
--
ALTER TABLE `rôle`
  ADD PRIMARY KEY (`idrole`);

--
-- Indexes for table `utilisateur`
--
ALTER TABLE `utilisateur`
  ADD PRIMARY KEY (`idutilisateur`);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `appartenir`
--
ALTER TABLE `appartenir`
  ADD CONSTRAINT `appartenir_ibfk_1` FOREIGN KEY (`idutilisateur`) REFERENCES `utilisateur` (`idutilisateur`),
  ADD CONSTRAINT `appartenir_ibfk_2` FOREIGN KEY (`idligue`) REFERENCES `ligue` (`idligue`);

--
-- Constraints for table `avoirunrole`
--
ALTER TABLE `avoirunrole`
  ADD CONSTRAINT `avoirunrole_ibfk_1` FOREIGN KEY (`idutilisateur`) REFERENCES `utilisateur` (`idutilisateur`),
  ADD CONSTRAINT `avoirunrole_ibfk_2` FOREIGN KEY (`idrole`) REFERENCES `rôle` (`idrole`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
