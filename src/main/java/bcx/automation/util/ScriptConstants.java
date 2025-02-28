package bcx.automation.util;

public class ScriptConstants {
    public static final String FIND_POTENTIAL_ELEMENT_SCRIPT = """
        (searchText) => {
            function normalizeText(text) {
                return text
                    .trim() // Supprime les espaces au début et à la fin
                    .replace(/\\s+/g, ' ') // Remplace plusieurs espaces par un seul
                    .normalize("NFD") // Décompose les caractères accentués (é → e + ́)
                    .replace(/\\p{Mn}/gu, '') // Supprime les accents
                    .toLowerCase(); // Convertit en minuscule
            }

            function levenshtein(a, b) {
                a = normalizeText(a);
                b = normalizeText(b);

                const matrix = [];
                for (let i = 0; i <= b.length; i++) {
                    matrix[i] = [i];
                }
                for (let j = 0; j <= a.length; j++) {
                    matrix[0][j] = j;
                }
                for (let i = 1; i <= b.length; i++) {
                    for (let j = 1; j <= a.length; j++) {
                        if (b.charAt(i - 1) === a.charAt(j - 1)) {
                            matrix[i][j] = matrix[i - 1][j - 1];
                        } else {
                            matrix[i][j] = Math.min(
                                matrix[i - 1][j - 1] + 1, // Remplacement
                                matrix[i][j - 1] + 1,     // Insertion
                                matrix[i - 1][j] + 1      // Suppression
                            );
                        }
                    }
                }
                const levenshteinScore = 1 - (matrix[b.length][a.length] / Math.max(a.length, b.length));
                console.log("levenstein " + matrix[b.length][a.length]);
                console.log("levenstein " + levenshteinScore);
                return levenshteinScore;
            }
        
            function jaroWinkler(s1, s2) {
                s1 = normalizeText(s1);
                s2 = normalizeText(s2);
        
                if (!s1 || !s2) return 0.0;
    
                let m = 0; // Nombre de correspondances
                let t = 0; // Nombre de transpositions
                let prefix = 0; // Longueur du préfixe commun
                let maxPrefix = 4; // Jaro-Winkler prend un préfixe max de 4
                let scalingFactor = 0.1; // Facteur d'ajustement de Winkler

                let matchDistance = Math.floor(Math.max(s1.length, s2.length) / 2) - 1;
                let s1Matches = new Array(s1.length).fill(false);
                let s2Matches = new Array(s2.length).fill(false);

                // Étape 1: Trouver les correspondances
                for (let i = 0; i < s1.length; i++) {
                    let start = Math.max(0, i - matchDistance);
                    let end = Math.min(i + matchDistance + 1, s2.length);

                    for (let j = start; j < end; j++) {
                        if (s2Matches[j]) continue;
                        if (s1[i] !== s2[j]) continue;
                        s1Matches[i] = s2Matches[j] = true;
                        m++;
                        break;
                    }
                }

                if (m === 0) return 0.0;

                // Étape 2: Compter les transpositions
                let k = 0;
                for (let i = 0; i < s1.length; i++) {
                    if (!s1Matches[i]) continue;
                    while (!s2Matches[k]) k++;
                    if (s1[i] !== s2[k]) t++;
                    k++;
                }
                t /= 2;

                // Étape 3: Calcul de la distance de Jaro
                let jaro = (m / s1.length + m / s2.length + (m - t) / m) / 3;

                // Étape 4: Appliquer l'amélioration Winkler si applicable
                for (let i = 0; i < Math.min(s1.length, s2.length, maxPrefix); i++) {
                    if (s1[i] !== s2[i]) break;
                    prefix++;
                }

                return jaro + (prefix * scalingFactor * (1 - jaro));
            }

            function similarityScore(str1, str2) {
                if (str1 == null || str2 == null) return 1;
                const jaroWinklerScore = jaroWinkler(str1, str2);
                const levenshteinScore = levenshtein(str1, str2);
                console.log(jaroWinklerScore + " - " + levenshteinScore);
                return (jaroWinklerScore * 0.6) + (levenshteinScore * 0.4);
            }


            function getPixelDistanceToLabel(input, element) {
                const inputRect = input.getBoundingClientRect();
                const labelRect = element.getBoundingClientRect();
                return Math.hypot(
                    (inputRect.left + inputRect.right) / 2 - (labelRect.left + labelRect.right) / 2,
                    (inputRect.top + inputRect.bottom) / 2 - (labelRect.top + labelRect.bottom) / 2
                );
            }

        function getNearbyText(element) {
            const nearbyTextMap = new Map();
        
            // Ajouter le texte de l'élément lui-même et l'élément DOM
            const textContent = element.textContent.trim();
            if (textContent) {
                nearbyTextMap.set(textContent, element);
            }
        
            // Chercher un parent pertinent (label, div, span, td)
            const parent = element.closest('label, div, span, td');
            if (parent) {
                const parentText = parent.textContent.trim();
                if (parentText) {
                    nearbyTextMap.set(parentText, parent);
                }
            }
        
            // Ajouter le texte du frère précédent
            const previousSibling = element.previousElementSibling;
            if (previousSibling) {
                const previousText = previousSibling.textContent.trim();
                if (previousText) {
                    nearbyTextMap.set(previousText, previousSibling);
                }
            }
        
            // Ajouter le texte du frère suivant
            const nextSibling = element.nextElementSibling;
            if (nextSibling) {
                const nextText = nextSibling.textContent.trim();
                if (nextText) {
                    nearbyTextMap.set(nextText, nextSibling);
                }
            }
        
            return nearbyTextMap;
        }

        function findClosestElementByText(searchText) {
          const allElements = document.querySelectorAll('{tagNames}');
          const visibleElements = Array.from(allElements).filter(element => {
              const style = window.getComputedStyle(element);
              return style.display !== 'none' && style.visibility !== 'hidden' && element.offsetParent !== null;
          });
        
          const matches = [];
        
          visibleElements.forEach(input => {
              const attributesToCheck = [{potentialAttributes}];
        
              attributesToCheck.forEach(attr => {
                  if (attr) {
                      const distance = similarityScore(searchText.toLowerCase(), attr.toLowerCase());
                      console.log("(attr) " + distance + " : " + attr);
                      matches.push({ input, distance, pixelDistance: 0, source: 'attribute', value: attr });
                  }
              });
        
              const nearbyText = getNearbyText(input);
              nearbyText.forEach((element, text) => {
                 const distance = similarityScore(searchText.toLowerCase(), text.toLowerCase());
                 const pixelDistance = getPixelDistanceToLabel(input, element);
                 console.log("(label) " + distance + " : " + text);
                 matches.push({ input, distance, pixelDistance, source: 'nearby', value: text });
             });
          });
        
          // Trier par distance similarité PUIS par proximité en pixels
          matches.sort((a, b) => b.distance - a.distance || a.pixelDistance - b.pixelDistance);
          return matches.length > 0 ? matches[0].input : null;
        };
    
    return findClosestElementByText(searchText)};
        """;
}
