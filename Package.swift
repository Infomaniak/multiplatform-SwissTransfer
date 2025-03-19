// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SwissTransfer-Multiplatform",
    platforms: [
        .iOS(.v14),
        .macOS(.v11)
    ],
    products: [
        .library(name: "STCore", targets: ["Core"]),
        .library(name: "STDatabase", targets: ["Database"]),
        .library(name: "STNetwork", targets: ["Network"])
    ],
    targets: [
        .binaryTarget(
            name: "Core",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.0.2/STCore.xcframework.zip",
            checksum: "56ae67d1a935cece3628f30c3c122b085a3bceba99b3daa5e9d1c9cf45d5d311"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.0.2/STDatabase.xcframework.zip",
            checksum: "22966a9d46da3c1595dc0a1e4134d225d5074503b17f748636ff84cda7638b21"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/3.0.2/STNetwork.xcframework.zip",
            checksum: "637ccfaa252931b87490a6d65ef8705b29b11dd02c8eb614136c4628bb948dd2"
        ),
    ]
)
