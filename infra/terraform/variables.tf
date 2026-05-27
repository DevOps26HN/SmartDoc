variable "resource_group_name" {
  default = "smartdoc-rg"
}

variable "location" {
  default = "West Europe"
}

variable "vm_name" {
  default = "smartdoc-vm"
}

variable "admin_username" {
  default = "azureuser"
}

variable "vm_size" {
  default = "Standard_B2s_v2"
}

variable "ssh_public_key_path" {
  default = "~/.ssh/id_rsa.pub"
}

variable "ssh_allowed_cidr" {
  default = "*"
}

variable "frontend_allowed_cidr" {
  default = "*"
}

variable "backend_allowed_cidr" {
  default = "*"
}