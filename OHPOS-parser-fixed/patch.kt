                        RecentOrderItem(
                            billWithItems = billWithItems,
                            hiddenInvoiceId = hiddenInvoiceId,
                            onClick = { rect -> 
                                hiddenInvoiceId = billWithItems.bill.id
                                selectedInvoiceId = billWithItems.bill.id
                                selectedInvoiceRect = rect
                            }
                        )
