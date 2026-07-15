# GoRules Integration Guide

This document explains how to modify business rules using GoRules UI and the rule files in this project.

## Overview

The Insurance Quote System uses GoRules Zen Engine for business rule management. Rules are stored as JSON Decision Model (JDM) files in `src/main/resources/rules/` and are automatically loaded and hot-reloaded when modified.

## Rule Files

### 1. eligibility.json
Determines if a customer is eligible for insurance coverage based on:
- **Age**: Must be 18 or older
- **Income**: Minimum annual income requirements
- **Coverage Type**: Different coverage tiers have different requirements

### 2. pricing.json
Calculates risk assessment and premium multiplier based on:
- **Age**: Younger drivers (under 25) have higher risk
- **Vehicle Type**: Sports cars have higher risk
- **Vehicle Value**: Higher value vehicles cost more to insure
- **Claim History**: More claims increase risk

### 3. discount.json
Applies discounts based on:
- **Premium Amount**: Higher premiums may qualify for loyalty discounts
- **Customer Tier**: Gold/Silver members get discounts
- **Years Active**: Longer customers get loyalty discounts
- **Claim Free Years**: No claims history reduces premium

## Modifying Rules

### Option 1: Using GoRules BRMS UI (Recommended)

1. **Access GoRules BRMS**
   - Deploy GoRules BRMS (see https://gorules.io for deployment options)
   - Open the BRMS UI in your browser

2. **Import Decision**
   - Click "Import" in the toolbar
   - Select the JSON file (e.g., `eligibility.json`)
   - The decision graph will be loaded into the editor

3. **Modify Rules**
   - Use the visual editor to:
     - Add/remove/modify decision tables
     - Change conditions and outputs
     - Add new nodes (expressions, switches, etc.)
   - Use the Simulator to test changes

4. **Export Decision**
   - Click "Export" in the toolbar
   - Choose "JSON Decision Model (.json)"
   - Save the file back to `src/main/resources/rules/`

### Option 2: Direct JSON Editing

1. **Edit the JSON file directly**
   - Open the rule file (e.g., `eligibility.json`)
   - Modify the `rules` array in the decision table node
   - Each rule has `inputEntries` (conditions) and `outputEntries` (results)

2. **Rule Structure**
   ```json
   {
     "inputEntries": [
       { "value": "< 18" },      // Condition: age less than 18
       { "value": "" },           // Empty = match any value
       { "value": "\"BASIC\"" }   // Condition: coverageType equals "BASIC"
     ],
     "outputEntries": [
       { "value": "false" },      // Output: eligible = false
       { "value": "\"REJECTED\"" }, // Output: status = "REJECTED"
       { "value": "\"Age must be 18 or older\"" } // Output: reason
     ]
   }
   ```

3. **Condition Syntax**
   - `>= 18` - Greater than or equal to 18
   - `< 25` - Less than 25
   - `"SPORTS"` - Equals string "SPORTS"
   - `> 50000` - Greater than 50000
   - `""` - Empty string matches any value

## Hot Reload

The application uses Java WatchService to monitor the rules directory for changes. When a rule file is modified:

1. The file watcher detects the change
2. The rule is automatically reloaded
3. No application restart is required
4. Changes take effect immediately

### How It Works

- `RuleManager` initializes a `WatchService` on the rules directory
- A background thread monitors for file changes
- When a `.json` file is modified/created/deleted, the rule is reloaded
- The decision cache is updated automatically

### Testing Hot Reload

1. Start the application
2. Modify a rule file (e.g., change age threshold in `eligibility.json`)
3. Wait 1-2 seconds for the file watcher to detect the change
4. Test the API - the new rule is immediately active

## API Usage

### Start a Quote Workflow

```bash
curl -X POST http://localhost:8080/api/v1/quotes \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUST-001",
    "productType": "AUTO",
    "age": 30,
    "annualIncome": 75000,
    "vehicleType": "SEDAN",
    "vehicleValue": 35000,
    "customerTier": "GOLD",
    "yearsActive": 5,
    "claimFreeYears": 3,
    "claimHistory": 0,
    "coverageType": "STANDARD"
  }'
```

### Example Responses

**Eligible Customer (Gold, Low Risk)**
```json
{
  "customerId": "CUST-001",
  "quoteId": "QUOTE-001",
  "status": "PERSISTED",
  "premiumAmount": 720.00,
  "coverageAmount": 50000,
  "riskScore": "LOW",
  "riskCategory": "LOW_RISK",
  "message": "Risk: LOW_RISK, Discount: 25%"
}
```

**Ineligible Customer (Under 18)**
```json
{
  "customerId": "CUST-002",
  "quoteId": "QUOTE-002",
  "status": "REJECTED",
  "premiumAmount": 0,
  "coverageAmount": 0,
  "riskScore": "N/A",
  "riskCategory": "INELIGIBLE",
  "message": "Age must be 18 or older"
}
```

## Troubleshooting

### Rules Not Loading

1. Check logs for errors:
   ```bash
   grep "Error loading rules" logs/insurance-quote-service.log
   ```

2. Verify file permissions on the rules directory

3. Ensure JSON files are valid (use a JSON validator)

### Hot Reload Not Working

1. Check if the file watcher is running:
   ```bash
   grep "File watcher started" logs/insurance-quote-service.log
   ```

2. Verify the rules directory path in `application.yml`:
   ```yaml
   gorules:
     rules:
       path: classpath:rules
   ```

3. Check for file system permissions

### Evaluation Errors

1. Check rule syntax:
   ```bash
   grep "Failed to evaluate decision" logs/insurance-quote-service.log
   ```

2. Verify input data matches expected types (numbers vs strings)

3. Test with GoRules Simulator in BRMS UI

## Best Practices

1. **Version Control**: Commit rule changes with meaningful messages
2. **Test First**: Use the Simulator in GoRules BRMS before deploying
3. **Gradual Changes**: Modify one rule at a time and test
4. **Document Changes**: Update this README when adding/modifying rules
5. **Monitor Logs**: Watch for evaluation errors after deploying changes

## Resources

- [GoRules Documentation](https://docs.gorules.io)
- [Java SDK Reference](https://docs.gorules.io/developers/sdks/java)
- [JDM Standard](https://docs.gorules.io/developers/jdm/standard)
- [Decision Tables](https://docs.gorules.io/learn/authoring/decision-tables)
