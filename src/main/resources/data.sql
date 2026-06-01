-- ─────────────────────────────────────────────────────────────
--  AdminDesk – Données de test
--  Demandes administratives fictives
-- ─────────────────────────────────────────────────────────────

INSERT INTO demandes (reference, nom_demandeur, email_demandeur, telephone_demandeur,
                      type, description, statut, priorite, agent_traitant,
                      commentaire_agent, date_creation, date_mise_a_jour)
VALUES

-- 1
('DEM-2025-00001', 'Martin Dupont', 'martin.dupont@email.fr', '06 12 34 56 78',
 'URBANISME', 'Par la présente, je demande l''obtention d''un permis de construire de mon bien situé 14 rue des Lilas, 75014 Paris pour les travaux suivants : Extension d''une maison individuelle d''une superficie de 25 m² sur la façade ouest, consistant en la création d''une véranda fermée avec toit vitré. Je joins les plans architecturaux ainsi que le formulaire Cerfa 13406. Les travaux sont prévus pour débuter en mars 2026.',
 'EN_COURS', 'HAUTE', 'Sophie Laurent',
 'Dossier complet, en attente de validation PLU.',
 CURRENT_TIMESTAMP - INTERVAL '15' DAY, CURRENT_TIMESTAMP - INTERVAL '2' DAY),

-- 2
('DEM-2025-00002', 'Amina Benali', 'amina.benali@gmail.com', '07 98 76 54 32',
 'AIDE_SOCIALE',
 'Suite à la perte de mon emploi il y a 3 mois, je me retrouve dans une situation financière très difficile. Je ne parviens plus à payer mon loyer de 850 euros mensuel et je risque une expulsion. J''ai deux enfants à charge âgés de 6 et 9 ans. Je sollicite une aide d''urgence ainsi qu''un accompagnement vers les dispositifs disponibles. Je suis actuellement bénéficiaire du RSA depuis le mois dernier.',
 'EN_ATTENTE', 'URGENTE', null, null,
 CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY),

-- 3
('DEM-2025-00003', 'Pierre Moreau', 'p.moreau@orange.fr', '06 55 44 33 22',
 'INSCRIPTION_SCOLAIRE',
 'Je souhaite inscrire ma fille Emma Moreau, née le 15 avril 2020, en classe de petite section de maternelle pour la rentrée de septembre 2026. Nous venons de nous installer au 8 avenue Victor Hugo dans votre commune. Je fournis le justificatif de domicile, le carnet de santé avec vaccinations à jour, le livret de famille et son numero de sécurité sociale 220059999999999. Merci de m''indiquer l''école de secteur correspondant à notre adresse.',
 'TRAITE', 'NORMALE', 'Jean-Claude Petit',
 'Inscription validée. École Jules Ferry secteur confirmé. Convocation envoyée par mail.',
 CURRENT_TIMESTAMP - INTERVAL '30' DAY, CURRENT_TIMESTAMP - INTERVAL '20' DAY),

-- 4
('DEM-2025-00004', 'Sylvie Duchamp', 'sylvie.duchamp@sfr.fr', '06 11 22 33 44',
 'ETAT_CIVIL',
 'Je souhaite obtenir un acte de naissance pour moi-même. Je suis née le 23 juin 1978 à Bordeaux (33). Cet acte m''est demandé dans le cadre d''une demande de passeport. Pouvez-vous m''indiquer la procédure et les délais ? Je suis disponible en semaine pour venir retirer le document en mairie si nécessaire. Ma commune de naissance est bien Bordeaux.',
 'TRAITE', 'BASSE', 'Marie Fontaine',
 'Réponse envoyée : redirection vers mairie de Bordeaux, acte de naissance non délivré par notre commune.',
 CURRENT_TIMESTAMP - INTERVAL '10' DAY, CURRENT_TIMESTAMP - INTERVAL '8' DAY),

-- 5
('DEM-2025-00005', 'Louis Kadier', 'louis.kadier@laposte.net', '07 60 50 40 30',
 'VOIRIE',
 'Bonjour, j''en ai vraiment marre !! Y''a un trou énorme dans la route devant chez moi rue du Commerce, ça fait des semaines et personne fait rien. L''autre jour une bagnole a failli se planter dedans, c''est super dangereux surtout le soir quand on voit rien. Faut faire quelque chose avant qu''il y ait un accident grave, c''est pas normal de payer des impôts pour des routes pareilles. Merci de vous en occuper vite.',
 'EN_COURS', 'HAUTE', 'Robert Garnier', null,
 CURRENT_TIMESTAMP - INTERVAL '5' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY),

-- 6
('DEM-2025-00006', 'Claire Tissot', 'c.tissot@hotmail.com', '06 33 44 55 66',
 'PERMIS',
 'Je gère une boulangerie artisanale au 5 place du Marché et je souhaite obtenir une autorisation d''occupation du domaine public pour installer une terrasse de 8 places devant mon établissement, du 1er avril au 30 septembre 2026. La terrasse ferait 4 mètres de longueur sur 2 mètres de profondeur. Je m''engage à respecter les règles de circulation piétonne et à démonter la terrasse chaque soir. Merci de me communiquer le formulaire et les tarifs applicables.',
 'EN_ATTENTE_PIECES', 'NORMALE', 'Sophie Laurent',
 'En attente du plan de masse et de l''attestation d''assurance responsabilité civile.',
 CURRENT_TIMESTAMP - INTERVAL '7' DAY, CURRENT_TIMESTAMP - INTERVAL '3' DAY),

-- 7
('DEM-2025-00007', 'François Leroy', 'francois.leroy@gmail.com', '06 77 88 99 00',
 'URBANISME',
 'Je voudrais savoir si ma parcelle cadastrale AB 0042 est constructible. J''ai hérité de ce terrain de 1200 m² en zone périurbaine et j''envisage d''y construire une maison individuelle. Le terrain est actuellement en friche. Avant de lancer une étude architecturale, je souhaite obtenir un certificat d''urbanisme d''information. Y a-t-il des contraintes particulières (zone inondable, monument historique, etc.) dont je devrais tenir compte ?',
 'EN_COURS', 'NORMALE', 'Thomas Bernard', null,
 CURRENT_TIMESTAMP - INTERVAL '12' DAY, CURRENT_TIMESTAMP - INTERVAL '4' DAY),

-- 8
('DEM-2025-00008', 'Nathalie Roux', 'nathalie.roux@wanadoo.fr', '07 11 00 99 88',
 'AIDE_SOCIALE',
 'Ma mère, Mme Jeanne Roux âgée de 84 ans, vit seule depuis le décès de mon père en 2024. Elle commence à avoir des difficultés pour les actes de la vie quotidienne (courses, ménage, repas). Nous habitons à 200 km et ne pouvons être présents en permanence. Je souhaite me renseigner sur les aides disponibles : aide à domicile, portage de repas, téléassistance. Quelles sont les démarches pour constituer un dossier APA ?',
 'EN_ATTENTE', 'NORMALE', null, null,
 CURRENT_TIMESTAMP - INTERVAL '3' DAY, CURRENT_TIMESTAMP - INTERVAL '3' DAY),

-- 9
('DEM-2025-00009', 'Ahmed Ziani', 'a.ziani@free.fr', '06 22 33 44 55',
 'ETAT_CIVIL',
 'Je souhaite effectuer une déclaration de naissance pour mon fils Karim Ziani, né le 10 janvier 2026 à la maternité Saint-Joseph de votre commune. Je suis dans les délais légaux de 5 jours. Je me présenterai avec le livret de famille, la pièce d''identité, et le certificat d''accouchement remis par la maternité. Quels sont vos horaires d''ouverture pour l''état civil ? Est-il possible de prendre rendez-vous en ligne ?',
 'TRAITE', 'HAUTE', 'Marie Fontaine',
 'Acte de naissance établi le 13/01/2026. Livret de famille mis à jour. Remis en main propre.',
 CURRENT_TIMESTAMP - INTERVAL '45' DAY, CURRENT_TIMESTAMP - INTERVAL '44' DAY),

-- 10
('DEM-2025-00010', 'Isabelle Mercier', 'isabelle.mercier@yahoo.fr', '06 44 55 66 77',
 'ENVIRONNEMENT',
 'Je constate depuis plusieurs semaines des dépôts sauvages d''ordures et d''encombrants au niveau du chemin rural des Charmes, à l''entrée du bois municipal. Des matelas, des meubles et des sacs plastiques s''accumulent et dégradent l''environnement. De plus, une odeur nauséabonde se dégage de ce lieu lors des fortes chaleurs. Je souhaite signaler ce problème et savoir si une intervention de nettoyage est prévue ainsi que les mesures pour éviter la récidive.',
 'EN_COURS', 'NORMALE', 'Robert Garnier',
 'Signalement transmis aux services techniques. Intervention prévue semaine 48.',
 CURRENT_TIMESTAMP - INTERVAL '9' DAY, CURRENT_TIMESTAMP - INTERVAL '2' DAY),

-- 11
('DEM-2025-00011', 'Lucas Girard', 'lucas.girard@gmail.com', '07 33 44 55 66',
 'INSCRIPTION_SCOLAIRE',
 'Mon fils Théo Girard, actuellement en CM2 à l''école privée Saint-Thomas, souhaite intégrer le collège public Henri Matisse en 6ème pour la rentrée 2026. Nous habitons dans le secteur de cet établissement. Je voudrais connaître les modalités d''inscription, les documents à fournir et les dates limites de dépôt du dossier. Y a-t-il des options particulières ou des sections bi-langues disponibles ?',
 'EN_ATTENTE', 'NORMALE', null, null,
 CURRENT_TIMESTAMP - INTERVAL '2' DAY, CURRENT_TIMESTAMP - INTERVAL '2' DAY),

-- 12
('DEM-2025-00012', 'Monique Blanc', 'monique.blanc@outlook.fr', '06 55 66 77 88',
 'VOIRIE',
 'Le lampadaire situé devant le numéro 17 rue des Acacias est en panne depuis plus de 3 semaines. Cette zone est particulièrement sombre la nuit et représente un risque pour la sécurité des piétons, notamment les personnes âgées et les enfants qui rentrent de l''école. J''ai déjà signalé ce problème par téléphone il y a deux semaines sans résultat. Je sollicite une intervention rapide.',
 'REJETE', 'NORMALE', 'Thomas Bernard',
 'Transféré au service intercommunal d''éclairage public – hors compétence municipale. Coordonnées transmises au demandeur.',
 CURRENT_TIMESTAMP - INTERVAL '22' DAY, CURRENT_TIMESTAMP - INTERVAL '18' DAY),

-- 13
('DEM-2025-00013', 'Patrick Duval', 'patrick.duval@sfr.fr', '07 66 77 88 99',
 'PERMIS',
 'Je souhaite organiser une brocante associative le dimanche 15 mars 2026 sur le parking du gymnase municipal, de 7h à 17h. Notre association loi 1901 "Les Amis du Quartier" organise cet événement chaque année. Nous attendons environ 60 exposants et 500 visiteurs. Nous nous chargeons du nettoyage en fin de journée. Merci de me confirmer la disponibilité du site et de me transmettre le formulaire de demande d''autorisation.',
 'EN_COURS', 'NORMALE', 'Jean-Claude Petit', null,
 CURRENT_TIMESTAMP - INTERVAL '6' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY),

-- 14
('DEM-2025-00014', 'Véronique Simon', 'veronique.simon@gmail.com', '06 88 77 66 55',
 'AIDE_SOCIALE',
 'Je suis mère célibataire avec 3 enfants (4, 7 et 11 ans) et je travaille à temps partiel comme aide à domicile. Malgré les aides CAF, je n''arrive pas à boucler mes fins de mois. Je sollicite une aide alimentaire ponctuelle ainsi qu''une information sur les épiceries sociales disponibles dans la commune. Je souhaite également savoir si je peux bénéficier d''une aide pour les fournitures scolaires à la rentrée prochaine.',
 'EN_ATTENTE', 'HAUTE', null, null,
 CURRENT_TIMESTAMP - INTERVAL '4' DAY, CURRENT_TIMESTAMP - INTERVAL '4' DAY),

-- 15
('DEM-2025-00015', 'Denis Lambert', 'denis.lambert@orange.fr', '06 99 88 77 66',
 'URBANISME',
 'Je prévois de ravaler la façade de mon immeuble de 8 logements situé au 3 boulevard Gambetta. Je souhaite changer la couleur actuelle beige pour un ton gris clair. L''immeuble est situé en zone ABF à proximité de l''église classée. Je voudrais savoir si je dois obtenir une autorisation préalable et si oui, quel formulaire utiliser. Quelles sont les couleurs autorisées dans ce périmètre de protection ?',
 'EN_ATTENTE_PIECES', 'NORMALE', 'Sophie Laurent',
 'Zone ABF confirmée. En attente de la notice descriptive et d''un échantillon de teinte.',
 CURRENT_TIMESTAMP - INTERVAL '8' DAY, CURRENT_TIMESTAMP - INTERVAL '5' DAY),

-- 16
('DEM-2025-00016', 'Fatima Okafor', 'fatima.okafor@gmail.com', '07 44 33 22 11',
 'ETAT_CIVIL',
 'Je souhaite obtenir un extrait d''acte de mariage pour mon mariage célébré le 20 juin 2005 dans votre mairie. Cet extrait m''est demandé par mon employeur dans le cadre d''une mutation professionnelle à l''étranger. J''ai besoin d''une copie intégrale avec filiation. Pouvez-vous me l''envoyer par courrier postal à l''adresse suivante : 25 rue de la Paix, 69002 Lyon ? Merci d''indiquer le délai de traitement.',
 'TRAITE', 'BASSE', 'Marie Fontaine',
 'Copie intégrale envoyée par courrier recommandé le 15/01/2026.',
 CURRENT_TIMESTAMP - INTERVAL '20' DAY, CURRENT_TIMESTAMP - INTERVAL '14' DAY),

-- 17
('DEM-2025-00017', 'Christophe Perrin', 'c.perrin@free.fr', '06 00 11 22 33',
 'ENVIRONNEMENT',
 'Un voisin a abattu sans autorisation apparente un chêne centenaire qui se trouvait sur sa propriété mais qui jouxtait l''espace public. Cet arbre était dans un état sanitaire parfait selon moi. Il ornait notre rue depuis des décennies et faisait partie du paysage du quartier. Existe-t-il une réglementation protégeant ce type d''arbre remarquable ? Une plainte peut-elle être déposée et si oui, comment ?',
 'EN_COURS', 'NORMALE', 'Thomas Bernard',
 'Vérification PLU en cours. Consultation du règlement du lotissement demandée.',
 CURRENT_TIMESTAMP - INTERVAL '11' DAY, CURRENT_TIMESTAMP - INTERVAL '3' DAY),

-- 18
('DEM-2025-00018', 'Martine Aubert', 'martine.aubert@wanadoo.fr', '07 55 44 33 22',
 'AIDE_SOCIALE',
 'Mon mari vient d''être hospitalisé pour une longue durée (minimum 6 mois) suite à un AVC. Je me retrouve seule à gérer le domicile et nos deux enfants adolescents. Je ne travaille pas et nous vivions de son salaire. Je suis complètement dépassée par les démarches administratives (CPAM, prévoyance, CAF). Pouvez-vous m''orienter vers un service d''accompagnement social ou une assistante sociale qui pourrait me guider ?',
 'EN_COURS', 'URGENTE', 'Sophie Laurent',
 'Rendez-vous fixé avec l''assistante sociale le 28/01/2026. Dossier d''urgence ouvert.',
 CURRENT_TIMESTAMP - INTERVAL '3' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY),

-- 19
('DEM-2025-00019', 'Alexandre Moulin', 'alexandre.moulin@gmail.com', '06 66 77 88 99',
 'VOIRIE',
 'Je signale un problème de stationnement anarchique devant l''entrée de l''école primaire Jean Jaurès lors des déposes et reprises des enfants. Des véhicules stationnent sur les passages piétons et la voie de bus, créant des situations dangereuses chaque matin entre 8h15 et 8h45 et le soir entre 16h30 et 17h. Des panneaux d''interdiction semblent insuffisants. Serait-il possible de prévoir une présence de la police municipale aux heures de pointe ?',
 'EN_ATTENTE', 'HAUTE', null, null,
 CURRENT_TIMESTAMP - INTERVAL '2' DAY, CURRENT_TIMESTAMP - INTERVAL '2' DAY),

-- 20
('DEM-2025-00020', 'Juliette Faure', 'juliette.faure@hotmail.com', '07 22 33 44 55',
 'PERMIS',
 'Je suis artiste plasticienne et je souhaite exposer mes œuvres dans l''espace public, plus précisément sur la place de la République, pendant 2 semaines en juillet 2026. L''exposition "Couleurs du Monde" comporterait une vingtaine de toiles de grand format montées sur structures légères. Je m''engage à obtenir toutes les assurances nécessaires. Quelle est la procédure pour obtenir l''autorisation d''occupation temporaire de l''espace public à des fins culturelles ?',
 'EN_ATTENTE', 'NORMALE', null, null,
 CURRENT_TIMESTAMP - INTERVAL '1' DAY, CURRENT_TIMESTAMP - INTERVAL '1' DAY);
