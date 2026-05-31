param(
    [string]$BaseUrl = "http://localhost:8083",
    [int]$AccountId = 2,
    [int]$CartId = 1,
    [int]$CustomerTradingId = 1,
    [string]$ApiKey = "KH3T_SHOP_KEY",
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Invoke-JsonApi {
    param(
        [Parameter(Mandatory = $true)][ValidateSet('GET', 'POST', 'PUT', 'DELETE')] [string]$Method,
        [Parameter(Mandatory = $true)][string]$Path,
        [object]$Body = $null,
        [hashtable]$Headers = @{}
    )

    $uri = "$BaseUrl$Path"
    Write-Host "[$Method] $uri" -ForegroundColor Cyan

    if ($DryRun) {
        if ($null -ne $Body) {
            Write-Host ("Request body:`n" + ($Body | ConvertTo-Json -Depth 10)) -ForegroundColor DarkGray
        }
        return $null
    }

    $params = @{
        Uri         = $uri
        Method      = $Method
        ErrorAction  = 'Stop'
        Headers      = $Headers
    }

    if ($null -ne $Body) {
        $params['ContentType'] = 'application/json'
        $params['Body'] = ($Body | ConvertTo-Json -Depth 10)
    }

    Invoke-RestMethod @params
}

Write-Host "Step 1: Read cart" -ForegroundColor Yellow
$cart = Invoke-JsonApi -Method GET -Path "/carts/account/$AccountId"

if (-not $DryRun) {
    $cartIdFromApi = $cart.result.id
    if ($cartIdFromApi) {
        $CartId = [int]$cartIdFromApi
    }
    Write-Host ("Cart result: " + ($cart | ConvertTo-Json -Depth 10)) -ForegroundColor Green
}

Write-Host "Step 2: Read selected cart details" -ForegroundColor Yellow
$selectedDetails = Invoke-JsonApi -Method GET -Path "/cart-details/cart/$CartId/selected"

if (-not $DryRun) {
    if (-not $selectedDetails -or $selectedDetails.Count -eq 0) {
        Write-Host "No selected items found. Auto-selecting all cart items for the test run..." -ForegroundColor DarkYellow
        $allDetails = Invoke-JsonApi -Method GET -Path "/cart-details/cart/$CartId"
        foreach ($detail in $allDetails) {
            Invoke-JsonApi -Method PUT -Path "/cart-details/$($detail.id)/select" -Body @{ selected = $true }
        }
        $selectedDetails = Invoke-JsonApi -Method GET -Path "/cart-details/cart/$CartId/selected"
    }

    Write-Host ("Selected items: " + ($selectedDetails | ConvertTo-Json -Depth 10)) -ForegroundColor Green
}

Write-Host "Step 3: Create order" -ForegroundColor Yellow
$orderRequest = [ordered]@{
    note              = "Kịch bản test thanh toán đơn hàng"
    customerTradingId  = $CustomerTradingId
    account_id        = $AccountId
    paymentMethod     = "BANK_TRANSFER"
}
$order = Invoke-JsonApi -Method POST -Path "/orders/create" -Body $orderRequest

if ($DryRun) {
    Write-Host "Dry run finished. No request was sent." -ForegroundColor Green
    return
}

Write-Host ("Order created: " + ($order | ConvertTo-Json -Depth 10)) -ForegroundColor Green

Write-Host "Step 4: Create invoice" -ForegroundColor Yellow
$invoiceRequest = [ordered]@{
    orderId        = $order.id
    paymentMethod  = "BANK_TRANSFER"
    paymentStatus  = "UNPAID"
}
$invoice = Invoke-JsonApi -Method POST -Path "/invoices" -Body $invoiceRequest
Write-Host ("Invoice created: " + ($invoice | ConvertTo-Json -Depth 10)) -ForegroundColor Green

Write-Host "Step 5: Send SePay callback" -ForegroundColor Yellow
$callbackBody = [ordered]@{
    gateway         = "SEPAY"
    transactionDate = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    accountNumber   = "970422"
    subAccount      = ""
    code            = $invoice.invoiceCode
    content         = "Thanh toan don hang $($invoice.invoiceCode)"
    transferType    = "in"
    description     = "Payment for order test"
    transferAmount  = [double]$invoice.totalAmount
    referenceCode   = "REF-$($order.orderCode)"
    accumulated     = [double]$invoice.totalAmount
    id              = 10001
}

$callbackHeaders = @{ Authorization = "Apikey $ApiKey" }
$callbackResult = Invoke-JsonApi -Method POST -Path "/api/v1/payment/sepay-callback" -Body $callbackBody -Headers $callbackHeaders
Write-Host ("SePay callback result: " + ($callbackResult | ConvertTo-Json -Depth 10)) -ForegroundColor Green

Write-Host "Step 6: Verify invoice status" -ForegroundColor Yellow
$verifiedInvoice = Invoke-JsonApi -Method GET -Path "/invoices/$($invoice.id)"
Write-Host ("Final invoice state: " + ($verifiedInvoice | ConvertTo-Json -Depth 10)) -ForegroundColor Green
