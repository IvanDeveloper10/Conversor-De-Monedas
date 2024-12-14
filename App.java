import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import java.util.Scanner;
import com.google.gson.Gson;

public class App {
    public static void main(String[] args) {
        String directionURI = "https://v6.exchangerate-api.com/v6/60e73e87cee8a4920c00482a/latest/USD";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(directionURI))
                .build();

        client.sendAsync(request, BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(response -> {
                    Gson gson = new Gson();
                    ExchangeRateResponse exchangeRate = gson.fromJson(response, ExchangeRateResponse.class);

                    if (!"success".equals(exchangeRate.getResult())) {
                        System.out.println("Error al obtener las tasas de cambio.");
                        return;
                    }

                    // Iniciar el menú interactivo
                    runMenu(exchangeRate.getConversion_rates());
                })
                .join();
    }

    private static void runMenu(Map<String, Double> rates) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // Mostrar el menú
            System.out.println("\n=== Conversor de Monedas ===");
            System.out.println("1. Realizar una conversión");
            System.out.println("2. Ver tasas disponibles");
            System.out.println("3. Salir");
            System.out.print("Seleccione una opción: ");

            int option = scanner.nextInt();
            scanner.nextLine(); // Limpiar el buffer

            switch (option) {
                case 1:
                    performConversion(rates, scanner);
                    break;

                case 2:
                    showRates(rates);
                    break;

                case 3:
                    System.out.println("¡Gracias por usar el conversor de monedas!");
                    scanner.close();
                    return;

                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }
        }
    }

    private static void performConversion(Map<String, Double> rates, Scanner scanner) {
        System.out.println("\nIngrese la moneda de origen (ej.: USD):");
        String fromCurrency = scanner.nextLine().toUpperCase();

        System.out.println("Ingrese la moneda de destino (ej.: EUR):");
        String toCurrency = scanner.nextLine().toUpperCase();

        System.out.println("Ingrese la cantidad a convertir:");
        double amount = scanner.nextDouble();
        scanner.nextLine(); // Limpiar el buffer

        // Validar que las monedas existan
        if (!rates.containsKey(fromCurrency) || !rates.containsKey(toCurrency)) {
            System.out.println("Moneda no válida. Por favor intente de nuevo.");
            return;
        }

        // Calcular el valor convertido
        double conversionRate = rates.get(toCurrency) / rates.get(fromCurrency);
        double convertedAmount = amount * conversionRate;

        // Mostrar el resultado
        System.out.printf("Tasa de conversión de %s a %s: %.4f%n", fromCurrency, toCurrency, conversionRate);
        System.out.printf("Monto convertido: %.2f %s%n", convertedAmount, toCurrency);
    }

    private static void showRates(Map<String, Double> rates) {
        System.out.println("\nTasas de cambio disponibles:");
        rates.forEach((currency, rate) -> System.out.printf("%s: %.4f%n", currency, rate));
    }
}

// Clase para mapear la respuesta de la API
class ExchangeRateResponse {
    private String result;
    private String base_code;
    private Map<String, Double> conversion_rates;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getBase_code() {
        return base_code;
    }

    public void setBase_code(String base_code) {
        this.base_code = base_code;
    }

    public Map<String, Double> getConversion_rates() {
        return conversion_rates;
    }

    public void setConversion_rates(Map<String, Double> conversion_rates) {
        this.conversion_rates = conversion_rates;
    }
}
