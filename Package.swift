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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.2.0/STCore.xcframework.zip",
            checksum: "455fa7c83e414869ebda705272acded2450adda01f6a806c2ca0d823b813cc95"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.2.0/STDatabase.xcframework.zip",
            checksum: "f8300887b6c0f80af83e3b2f790bd7a78dec0c8298d62b922b981d8e5169f53b"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/5.2.0/STNetwork.xcframework.zip",
            checksum: "f18354920ae46e4cdde35045dbfdc8791b57e110e45420c61eb63eabc631aaae"
        ),
    ]
)
